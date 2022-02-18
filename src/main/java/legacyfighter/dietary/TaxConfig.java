package legacyfighter.dietary;

import javax.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
public class TaxConfig {

    @Id
    @GeneratedValue
    private Long id;
    private String description;
    private String countryReason;
    private String countryCode;
    private Instant lastModifiedDate;
    private int currentRulesCount;
    private int maxRulesCount;

    @OneToMany(cascade = CascadeType.ALL)
    private List<TaxRule> taxRules = new ArrayList<>();

    private TaxConfig(String countryCode, int maxRulesCount) {
        if (countryCode == null || countryCode.equals("") || countryCode.length() == 1) {
            throw new IllegalStateException("Invalid country code");
        }

        this.countryCode = countryCode;
        this.maxRulesCount = maxRulesCount;
    }

    public static TaxConfig from(String countryCode, int maxRulesCount) {
        return new TaxConfig(countryCode, maxRulesCount);
    }

    public void addTaxRule(TaxRule taxRule, Instant moment) {
        if (taxRules.size() >= maxRulesCount) {
            throw new IllegalStateException("Cannot add more rules");
        }
        taxRules.add(taxRule);
        lastModifiedDate = moment;
        currentRulesCount++;
    }

    public void removeTaxRule(TaxRule taxRule, Instant moment) {
        if (taxRules.contains(taxRule)) {
            if (taxRules.size() == 1) {
                throw new IllegalStateException("Last rule in country config");
            }
            taxRules.remove(taxRule);
            lastModifiedDate = moment;
            currentRulesCount--;
        }
    }

    public String getDescription() {
        return description;
    }

    public String getCountryReason() {
        return countryReason;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Instant getLastModifiedDate() {
        return lastModifiedDate;
    }

    public int getCurrentRulesCount() {
        return currentRulesCount;
    }

    public int getMaxRulesCount() {
        return maxRulesCount;
    }

    public void setMaxRulesCount(int maxRulesCount) {
        this.maxRulesCount = maxRulesCount;
    }

    public List<TaxRule> getTaxRules() {
        return taxRules;
    }

    public void setTaxRules(List<TaxRule> taxRules) {
        this.taxRules = taxRules;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaxConfig taxConfig = (TaxConfig) o;
        return id.equals(taxConfig.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public Long getId() {
        return id;
    }
}
