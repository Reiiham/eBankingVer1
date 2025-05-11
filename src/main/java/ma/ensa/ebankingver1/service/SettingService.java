package ma.ensa.ebankingver1.service;

import ma.ensa.ebankingver1.model.GlobalSetting;
import ma.ensa.ebankingver1.repository.SettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SettingService {

    @Autowired
    private SettingRepository globalSettingRepository;

    public String getSettingValue(String key) {
        return globalSettingRepository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found"))
                .getValue();
    }

    public GlobalSetting addSetting(GlobalSetting setting) {
        Optional<GlobalSetting> existingSetting = globalSettingRepository.findByKey(setting.getKey());
        if (existingSetting.isPresent()) {
            throw new IllegalArgumentException("Un paramètre avec cette clé existe déjà.");
        }
        return globalSettingRepository.save(setting);
    }

    public GlobalSetting updateSetting(GlobalSetting setting) {
        Optional<GlobalSetting> existingSetting = globalSettingRepository.findById(setting.getId());
        if (existingSetting.isEmpty()) {
            throw new IllegalArgumentException("Setting not found");
        }
        GlobalSetting current = existingSetting.get();
        current.setKey(setting.getKey() != null ? setting.getKey() : current.getKey());
        current.setValue(setting.getValue() != null ? setting.getValue() : current.getValue()); // Garder la valeur existante si null
        current.setCategory(setting.getCategory() != null ? setting.getCategory() : current.getCategory());
        current.setLabel(setting.getLabel() != null ? setting.getLabel() : current.getLabel());
        current.setDescription(setting.getDescription() != null ? setting.getDescription() : current.getDescription());
        return globalSettingRepository.save(current);
    }
    public List<GlobalSetting> getAllSettings() {
        return globalSettingRepository.findAll();
    }

    public GlobalSetting getSetting(String key) {
        return globalSettingRepository.findByKey(key)
                .orElseThrow(() -> new IllegalArgumentException("Setting not found"));
    }
    public void deleteSetting(Long id) {
        if (!globalSettingRepository.existsById(id)) {
            throw new IllegalArgumentException("Setting not found");
        }
        globalSettingRepository.deleteById(id);
    }
}
