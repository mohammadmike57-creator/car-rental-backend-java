package com.carrental.backend.service;

import com.carrental.backend.model.AppData;
import com.carrental.backend.repository.AppDataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StateService {

    private final AppDataRepository appDataRepository;
    private final ObjectMapper objectMapper;

    private static final String INITIAL_STATE = "{\"reservations\":{},\"sources\":[],\"fleet\":[],\"companyDetails\":{\"name\":\"\",\"subName\":\"\",\"address\":\"\",\"phone\":\"\",\"email\":\"\",\"taxNumber\":\"\",\"requirePaymentApproval\":false},\"trafficTickets\":[],\"vehicleDamages\":[],\"users\":[],\"expenses\":[],\"rentalLocations\":[],\"messages\":[],\"invoices\":[],\"availableExtras\":[],\"franchisePayments\":[],\"activityLog\":[],\"aggregators\":[],\"stopSales\":[],\"years\":[]}";

    public String getFullState() {
        AppData appData = appDataRepository.findMain()
                .orElseGet(() -> {
                    AppData newData = new AppData();
                    newData.setId("main");
                    newData.setData(INITIAL_STATE);
                    return appDataRepository.save(newData);
                });
        return appData.getData();
    }

    public void mergeState(String updatesJson) {
        AppData appData = appDataRepository.findMain()
                .orElseThrow(() -> new RuntimeException("App data not found"));

        try {
            JsonNode current = objectMapper.readTree(appData.getData());
            JsonNode updates = objectMapper.readTree(updatesJson);
            JsonNode merged = deepMerge(current, updates);
            appData.setData(objectMapper.writeValueAsString(merged));
            appDataRepository.save(appData);
        } catch (Exception e) {
            throw new RuntimeException("Failed to merge state", e);
        }
    }

    private JsonNode deepMerge(JsonNode source, JsonNode target) {
        if (source.isObject() && target.isObject()) {
            ObjectNode merged = objectMapper.createObjectNode();
            merged.setAll((ObjectNode) source);
            target.fields().forEachRemaining(entry -> {
                String key = entry.getKey();
                JsonNode targetValue = entry.getValue();
                JsonNode sourceValue = source.get(key);
                if (sourceValue != null && sourceValue.isObject() && targetValue.isObject()) {
                    merged.set(key, deepMerge(sourceValue, targetValue));
                } else {
                    merged.set(key, targetValue);
                }
            });
            return merged;
        }
        return target;
    }
}
