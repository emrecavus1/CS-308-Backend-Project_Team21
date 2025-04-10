package com.cs308.backend.services;

import org.springframework.http.ResponseEntity;
import com.cs308.backend.models.User;
import com.cs308.backend.repositories.UserRepository;
import com.cs308.backend.services.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserChecks {
    private final UserRepository userRepository;
    private final UserService userService;


    public UserChecks(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    public enum TurkishCities {
        ADANA, ADIYAMAN, AFYONKARAHISAR, AĞRI, AMASYA, ANKARA, ANTALYA, ARTVİN, AYDIN,
        BALIKESİR, BİLECİK, BİNGÖL, BİTLİS, BOLU, BURDUR, BURSA, ÇANAKKALE, ÇANKIRI,
        ÇORUM, DENİZLİ, DİYARBAKIR, EDİRNE, ELAZIĞ, ERZİNCAN, ERZURUM, ESKİŞEHİR,
        GAZİANTEP, GİRESUN, GÜMÜŞHANE, HAKKARİ, HATAY, ISPARTA, MERSİN, İSTANBUL,
        İZMİR, KARS, KASTAMONU, KAYSERİ, KIRKLARELİ, KIRŞEHİR, KOCAELİ, KONYA, KÜTAHYA,
        MALATYA, MANİSA, KARAMAN, KIRIKKALE, BATMAN, ŞIRNAK, BARTIN, ARDAHAN, IĞDIR,
        YALOVA, KARABÜK, KİLİS, OSMANİYE, DÜZCE
    }


    private boolean isValidTurkishCity(String city) {
        try {
            TurkishCities.valueOf(city.toUpperCase()); // Convert to uppercase and check in enum
            return true;
        } catch (IllegalArgumentException e) {
            return false; // City not found
        }
    }

    public String formatName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return name; // Return as is if null or empty
        }
        return name.substring(0, 1).toUpperCase() + name.substring(1).toLowerCase();
    }

    private boolean isValidName(String name) {
        return name.matches("^[A-Za-z-]+$");
    }

    public ResponseEntity<String> emailChecks (String email) {
        String[] emailTypes = {"outlook", "gmail", "hotmail", "yahoo", "icloud", "sabanciuniv"};
        String[] emailEndings = {".com", ".edu", ".org"};
        if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return ResponseEntity.badRequest().body("Invalid email format");
        }

        boolean typeFound = false;
        for (String type : emailTypes) {
            if (email.contains(type)) {
                if (typeFound) {
                    return ResponseEntity.badRequest().body("Email must contain exactly one email type (e.g., gmail, outlook, etc.).");
                }
                typeFound = true;
            }
        }
        if (!typeFound) {
            return ResponseEntity.badRequest().body("Email must contain one of the allowed email types.");
        }

        // Ensure email ends with exactly one email ending
        boolean endingFound = false;
        for (String ending : emailEndings) {
            if (email.endsWith(ending)) {
                if (endingFound) {
                    return ResponseEntity.badRequest().body("Email must end with exactly one of the allowed endings (.com, .edu, .tr, .org).");
                }
                endingFound = true;
            }
        }
        if (!endingFound) {
            return ResponseEntity.badRequest().body("Email must end with .com, .edu, or .org");
        }
        if (userService.isEmailTaken(email)) {
            return ResponseEntity.badRequest().body("Email already exists");
        }
        return ResponseEntity.ok("");
    }

    public ResponseEntity<String> passwordChecks (String password) {
        if (password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body("Password must be at least 8 characters long");
        }

        String passwordPattern = "^(?=.*[0-9])(?=.*[.!@#$%^&*()_+=-])(?=.*[A-Z])(?=.*[a-z]).*$";

        if (!password.matches(passwordPattern)) {
            return ResponseEntity.badRequest().body("Password must contain one number & special character & one uppercase letter & one lowercase letter");
        }
        return ResponseEntity.ok("");
    }

    public ResponseEntity<String> nameChecks (String name, String surname) {
        if (name == null || name.length() < 2) {
            return ResponseEntity.badRequest().body("Name must be at least 2 characters long");
        }

        if (!isValidName(name)) {
            return ResponseEntity.badRequest().body("Name must contain only alphabetic letters and '-'");
        }

        if (surname == null || surname.length() < 2) {
            return ResponseEntity.badRequest().body("Surname must be at least 2 characters long");
        }

        if (!isValidName(surname)) {
            return ResponseEntity.badRequest().body("Surname must contain only alphabetic letters and '-'");
        }

        return ResponseEntity.ok("");

    }

    public ResponseEntity<String> roleChecks (String role) {
        if (role == null) {
            return ResponseEntity.badRequest()
                    .body("Role is null. Must be Customer, Product Manager, or Sales Manager.");
        }

        String[] typeOfRoles = {"Customer", "Product Manager", "Sales Manager"};

        boolean roleFound = false;

        for (String theRole : typeOfRoles) {
            if (role.equalsIgnoreCase(theRole)) {
                roleFound = true;
                break;
            }
        }
        if (!roleFound) {
            return ResponseEntity.badRequest().body("Role must be either Customer, Product Manager, or Sales Manager");
        }

        return ResponseEntity.ok("");
    }

    public ResponseEntity<String> cityChecks (String city) {
        if (!isValidTurkishCity(city)) {
            return ResponseEntity.badRequest().body("Invalid city. Please enter a valid city in Turkey.");
        }
        return ResponseEntity.ok("");
    }

    public ResponseEntity<String> addressChecks (String address) {
        if (address == null || address.length() < 15) {
            return ResponseEntity.badRequest().body("Address must be at least 15 characters long");
        }
        return ResponseEntity.ok("");
    }

    public ResponseEntity<String> phoneNumberChecks (String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.matches("\\d{11}")) {
            return ResponseEntity.badRequest().body("Phone number must be exactly 11 digits and contain only numbers.");
        }

        if (!phoneNumber.startsWith("0"))
        {
            return ResponseEntity.badRequest().body("Phone number must start with 0");
        }
        return ResponseEntity.ok("");
    }

}
