package ma.ensa.ebankingver1.controller;

import jakarta.validation.Valid;
import ma.ensa.ebankingver1.model.GlobalSetting;
import ma.ensa.ebankingver1.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/settings")
public class SettingsController {

    @Autowired
    private SettingService settingService;

    public SettingsController() {
        System.out.println("SettingsController initialized");
    }

    @GetMapping
    public List<GlobalSetting> getAllSettings() {
        System.out.println("Received GET request for /api/admin/settings");
        return settingService.getAllSettings();
    }

    @PostMapping
    public ResponseEntity<GlobalSetting> addSetting(@RequestBody GlobalSetting setting) {
        try {
            System.out.println("Received POST request for /api/admin/settings");
            GlobalSetting newSetting = settingService.addSetting(setting);
            return ResponseEntity.status(HttpStatus.CREATED).body(newSetting);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<GlobalSetting> updateSetting(@PathVariable("id") Long id, @Valid @RequestBody GlobalSetting setting) {
        try {
            System.out.println("Received PUT request for /api/admin/settings/" + id);
            setting.setId(id);
            GlobalSetting updatedSetting = settingService.updateSetting(setting);
            return ResponseEntity.ok(updatedSetting);
        } catch (IllegalArgumentException e) {
            System.err.println("Setting not found for id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            System.err.println("Error updating setting: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSetting(@PathVariable("id") Long id) {
        try {
            System.out.println("Received DELETE request for /api/admin/settings/" + id);
            settingService.deleteSetting(id);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            System.err.println("Setting not found for id: " + id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("Error deleting setting: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}