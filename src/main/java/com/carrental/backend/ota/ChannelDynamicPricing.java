package com.carrental.backend.ota;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.BiFunction;

/**
 * Rate &amp; price rules (OTA XML → Rate rules), calculated day by day. Same calculation as the
 * website's services/dynamicPricing.ts, which the preview uses; the tests of both use the same cases.
 *
 *   1. base daily rate: the published rate of the day's period, for the whole rental's length
 *   2. seasonal / event rule: the highest-priority rule replaces the rate (all of them only when stacking is on)
 *   3. last-minute rule by lead time (optionally split by cars available)
 *   4. availability rule (not again when the last-minute rule already priced availability)
 *   5. minimum / maximum daily price
 *
 * Used only for channels whose rate mode is "dynamic" or "restricted" (seasonal rules only);
 * channels on "net" (the default) are priced exactly as before.
 */
public final class ChannelDynamicPricing {

    private ChannelDynamicPricing() {}

    public record Day(LocalDate date, double base, double rate, String rule, List<String> steps) {}
    public record Result(boolean bookable, String reason, int days, List<Day> perDay, double total, List<String> notes) {}

    private static final List<String> EVENT_KINDS = List.of("eid_fitr", "eid_adha", "ramadan", "christmas", "new_year", "national", "custom");
    private static final List<String> HIGH_KINDS = List.of("high", "peak");

    /** The channel's rate mode: net (default), dynamic or restricted. */
    public static String mode(JsonNode channel) {
        String m = channel.path("pricingMode").asText("net");
        return "dynamic".equals(m) || "restricted".equals(m) ? m : "net";
    }

    /** broker (default), ota, corporate, travel_agent, website, app or walk_in. */
    public static String channelType(JsonNode channel) {
        String t = channel.path("channelType").asText("broker");
        return List.of("website", "app", "walk_in", "broker", "ota", "corporate", "travel_agent").contains(t) ? t : "broker";
    }

    /** The published rules, or null when none are published. */
    public static JsonNode published(JsonNode state) {
        JsonNode p = state == null ? null : state.path("channelPricing").path("published");
        return p != null && p.isObject() ? p : null;
    }

    /** True when the last-minute rule prices this channel (the broker's own early / last-minute terms are then not added again). */
    public static boolean leadTimeActive(JsonNode cfg, String channelType, String mode) {
        return cfg != null && "dynamic".equals(mode) && cfg.path("leadTime").path("enabled").asBoolean(false)
            && listHas(cfg.path("leadTime").path("channels"), channelType);
    }

    public static double applyAdjust(double rate, JsonNode a) {
        double v = a.path("value").asDouble(0);
        return switch (a.path("type").asText("none")) {
            case "percent" -> rate * (1 + v / 100);
            case "amount" -> rate + v;
            case "fixed" -> v;
            default -> rate;
        };
    }

    static String describe(JsonNode a) {
        double v = a.path("value").asDouble(0);
        String n = num(v);
        return switch (a.path("type").asText("none")) {
            case "percent" -> (v >= 0 ? "+" : "") + n + "%";
            case "amount" -> (v >= 0 ? "+" : "") + n + " a day";
            case "fixed" -> n + " a day";
            default -> "no change";
        };
    }

    public static Result price(JsonNode cfg, String group, String location, String channelType, LocalDateTime pickup, LocalDateTime dropOff,
                               LocalDateTime now, int available, BiFunction<LocalDate, Integer, Double> baseDaily, int graceMinutes, String mode) {
        int days = OtaXml.rentalDays(pickup, dropOff, graceMinutes);
        double leadHours = Duration.between(now, pickup).toMinutes() / 60.0;
        List<String> notes = new ArrayList<>();
        if (available <= 0) return new Result(false, "No car of this group is free for these dates", days, List.of(), 0, notes);

        boolean full = "dynamic".equals(mode);
        boolean leadOn = full && cfg.path("leadTime").path("enabled").asBoolean(false) && listHas(cfg.path("leadTime").path("channels"), channelType);
        boolean availOn = full && cfg.path("availability").path("enabled").asBoolean(false) && listHas(cfg.path("availability").path("channels"), channelType);
        JsonNode lead = null, leadBand = null;
        if (leadOn) {
            for (JsonNode t : cfg.path("leadTime").path("tiers")) {
                double from = t.path("fromHours").asDouble(0);
                boolean open = !t.path("toHours").isNumber();
                if (leadHours >= from && (open || leadHours < t.path("toHours").asDouble())) { lead = t; break; }
            }
            if (lead != null) for (JsonNode b : lead.path("byAvailability")) if (inBand(b, available)) { leadBand = b; break; }
        }
        Double minDaily = optional(cfg.path("limits").path("minDaily")), maxDaily = optional(cfg.path("limits").path("maxDaily"));
        JsonNode gl = cfg.path("limits").path("groups").path(group.toUpperCase(Locale.ROOT));
        if (gl.isObject()) {
            if (optional(gl.path("minDaily")) != null) minDaily = optional(gl.path("minDaily"));
            if (optional(gl.path("maxDaily")) != null) maxDaily = optional(gl.path("maxDaily"));
        }
        List<Integer> weekend = new ArrayList<>();
        if (cfg.path("weekendDays").isArray()) cfg.path("weekendDays").forEach(d -> weekend.add(d.asInt()));
        else { weekend.add(5); weekend.add(6); }

        List<Day> out = new ArrayList<>();
        double total = 0;
        for (int i = 0; i < days; i++) {
            LocalDate date = pickup.toLocalDate().plusDays(i);
            Double base = baseDaily.apply(date, days);
            if (base == null) return new Result(false, "No published rate covers " + date, days, out, 0, notes);
            double rate = base;
            List<String> steps = new ArrayList<>();

            JsonNode rule = null;
            if (cfg.path("seasonal").path("enabled").asBoolean(false) && !"net".equals(mode)) {
                List<JsonNode> rules = rulesFor(cfg, date, group, location, channelType, days);
                rule = rules.isEmpty() ? null : rules.get(0);
                boolean stack = cfg.path("seasonal").path("stack").asBoolean(false);
                for (JsonNode r : stack ? rules : rules.subList(0, Math.min(1, rules.size()))) {
                    double to = applyAdjust(rate, r.path("adjust"));
                    steps.add(r.path("name").asText("Rule") + " (" + describe(r.path("adjust")) + "): " + money(rate) + " → " + money(to));
                    rate = to;
                }
            }
            if (lead != null) {
                JsonNode a = leadBand != null ? leadBand.path("adjust") : lead.path("adjust");
                if (!"none".equals(a.path("type").asText("none"))) {
                    double to = applyAdjust(rate, a);
                    if (round(to) != round(rate)) steps.add("Last minute (" + describe(a) + "): " + money(rate) + " → " + money(to));
                    rate = to;
                }
            }
            if (availOn && leadBand == null) {
                int dow = date.getDayOfWeek().getValue() % 7;   // 0 = Sunday, as in JavaScript
                String kind = rule == null ? "" : rule.path("kind").asText("");
                for (JsonNode t : cfg.path("availability").path("tiers")) {
                    if (!inBand(t, available) || !listHasText(t.path("locations"), location)) continue;
                    String when = t.path("when").asText("all");
                    boolean applies = "all".equals(when) || ("weekend".equals(when) && weekend.contains(dow))
                        || ("event".equals(when) && EVENT_KINDS.contains(kind)) || ("high".equals(when) && HIGH_KINDS.contains(kind));
                    if (!applies) continue;
                    if (!"none".equals(t.path("adjust").path("type").asText("none"))) {
                        double to = applyAdjust(rate, t.path("adjust"));
                        if (round(to) != round(rate)) steps.add("Availability (" + describe(t.path("adjust")) + "): " + money(rate) + " → " + money(to));
                        rate = to;
                    }
                    Double tMin = optional(t.path("minDaily")), tMax = optional(t.path("maxDaily"));
                    if (tMin != null && rate < tMin) { steps.add("Availability minimum " + money(tMin)); rate = tMin; }
                    if (tMax != null && rate > tMax) { steps.add("Availability maximum " + money(tMax)); rate = tMax; }
                    break;
                }
            }
            if (minDaily != null && rate < minDaily) { steps.add("Minimum daily price " + money(minDaily)); rate = minDaily; }
            if (maxDaily != null && rate > maxDaily) { steps.add("Maximum daily price " + money(maxDaily)); rate = maxDaily; }
            rate = Math.max(0, round(rate));
            out.add(new Day(date, round(base), rate, rule == null ? null : rule.path("name").asText(null), steps));
            total += rate;
        }
        if (leadBand != null) notes.add("Availability is priced inside the last-minute rule, so the availability rule is not applied again.");
        return new Result(true, null, days, out, round(total), notes);
    }

    /** Seasonal rules for one day, highest priority first (list order breaks ties). */
    static List<JsonNode> rulesFor(JsonNode cfg, LocalDate date, String group, String location, String channelType, int days) {
        String d = date.toString();
        List<JsonNode> rules = new ArrayList<>();
        for (JsonNode r : cfg.path("seasonal").path("rules")) {
            if (!r.path("active").asBoolean(true)) continue;
            if (r.path("from").asText("").compareTo(d) > 0 || d.compareTo(r.path("to").asText("")) > 0) continue;
            if (!listHasText(r.path("groups"), group) || !listHasText(r.path("locations"), location) || !listHas(r.path("channels"), channelType)) continue;
            boolean blocked = false;
            for (JsonNode b : r.path("blackout")) if (d.equals(b.asText())) { blocked = true; break; }
            if (blocked) continue;
            int min = r.path("minDays").asInt(0), max = r.path("maxDays").asInt(0);
            if ((min > 0 && days < min) || (max > 0 && days > max)) continue;
            rules.add(r);
        }
        List<JsonNode> sorted = new ArrayList<>(rules);
        sorted.sort(Comparator.comparingDouble(r -> r.path("priority").asDouble(99)));   // stable: list order breaks ties
        return sorted;
    }

    /** "9 days × 30.00, 3 days × 45.00 (Eid Al-Fitr)". */
    public static String summary(List<Day> perDay) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < perDay.size()) {
            Day d = perDay.get(i);
            int n = 1;
            while (i + n < perDay.size() && perDay.get(i + n).rate() == d.rate() && java.util.Objects.equals(perDay.get(i + n).rule(), d.rule())) n++;
            if (sb.length() > 0) sb.append(", ");
            sb.append(n).append(n == 1 ? " day × " : " days × ").append(money(d.rate()));
            if (d.rule() != null) sb.append(" (").append(d.rule()).append(')');
            i += n;
        }
        return sb.toString();
    }

    private static boolean inBand(JsonNode b, int n) {
        return n >= b.path("minCars").asInt(0) && (!b.path("maxCars").isNumber() || n <= b.path("maxCars").asInt());
    }

    private static boolean listHas(JsonNode list, String v) {
        if (list == null || !list.isArray() || list.isEmpty()) return true;
        for (JsonNode x : list) if (v.equals(x.asText())) return true;
        return false;
    }

    private static boolean listHasText(JsonNode list, String v) {
        if (list == null || !list.isArray() || list.isEmpty()) return true;
        for (JsonNode x : list) if (v.trim().equalsIgnoreCase(x.asText("").trim())) return true;
        return false;
    }

    private static Double optional(JsonNode n) { return n != null && n.isNumber() ? n.asDouble() : null; }
    static double round(double v) { return Math.round(v * 100) / 100.0; }
    private static String money(double v) { return String.format(Locale.ROOT, "%.2f", round(v)); }
    private static String num(double v) { return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v); }
}
