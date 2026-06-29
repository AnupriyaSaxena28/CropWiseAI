package com.cropwise.backend.service;

import com.cropwise.backend.dto.SchemeDto;
import org.springframework.stereotype.Service;
import java.util.List;

/** Curated directory of major Indian agricultural schemes. */
@Service
public class SchemesService {

    public List<SchemeDto> all() {
        return List.of(
            new SchemeDto("pm-kisan", "PM-KISAN",
                "Ministry of Agriculture & Farmers Welfare",
                "Income support of ₹6,000/year to all landholding farmer families, paid in three equal instalments.",
                List.of("₹6,000 per year direct benefit transfer", "Paid in 3 instalments of ₹2,000",
                        "Directly credited to bank account"),
                List.of("Landholding farmer family", "Valid Aadhaar", "Bank account linked to Aadhaar"),
                "https://pmkisan.gov.in", null, true),
            new SchemeDto("pmfby", "PMFBY (Crop Insurance)",
                "Ministry of Agriculture & Farmers Welfare",
                "Pradhan Mantri Fasal Bima Yojana provides comprehensive crop insurance against natural calamities, pests and diseases.",
                List.of("Low premium: 2% Kharif, 1.5% Rabi, 5% commercial/horticultural",
                        "Full sum insured on crop loss", "Covers prevented sowing & post-harvest losses"),
                List.of("All farmers including sharecroppers and tenant farmers", "Notified crops in notified areas"),
                "https://pmfby.gov.in", null, true),
            new SchemeDto("kcc", "Kisan Credit Card (KCC)",
                "Department of Financial Services",
                "Provides farmers timely access to short-term credit for cultivation and other needs at low interest.",
                List.of("Credit up to ₹3 lakh at 7% interest", "3% interest subvention on prompt repayment",
                        "Covers crop, post-harvest and consumption needs"),
                List.of("All farmers — individual / joint cultivators", "Tenant farmers and SHGs eligible"),
                "https://www.myscheme.gov.in/schemes/kcc", null, true),
            new SchemeDto("soil-health-card", "Soil Health Card",
                "Department of Agriculture & Cooperation",
                "Provides farmers a soil health report with crop-wise nutrient recommendations to optimise fertiliser use.",
                List.of("Free soil testing every 2 years", "Crop-wise nutrient & fertiliser recommendations",
                        "Reduces input cost and improves yield"),
                List.of("All farmers with cultivable land"),
                "https://soilhealth.dac.gov.in", null, true),
            new SchemeDto("pkvy", "Paramparagat Krishi Vikas Yojana",
                "Ministry of Agriculture & Farmers Welfare",
                "Promotes organic farming through cluster approach and PGS certification.",
                List.of("₹50,000/hectare over 3 years", "Support for organic inputs and certification",
                        "Premium price realisation for organic produce"),
                List.of("Farmers willing to adopt organic farming in clusters"),
                "https://pgsindia-ncof.gov.in", null, true),
            new SchemeDto("pm-kusum", "PM-KUSUM (Solar)",
                "Ministry of New & Renewable Energy",
                "Supports installation of solar pumps and grid-connected solar power plants for farmers.",
                List.of("Up to 60% subsidy on solar pumps", "Extra income by selling surplus solar power",
                        "Reduces diesel dependence and irrigation cost"),
                List.of("Individual farmers", "FPOs, cooperatives and panchayats"),
                "https://pmkusum.mnre.gov.in", null, true)
        );
    }
}
