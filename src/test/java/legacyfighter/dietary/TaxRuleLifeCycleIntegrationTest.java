package legacyfighter.dietary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class TaxRuleLifeCycleIntegrationTest {

    static Instant _1989_12_12 = LocalDateTime.of(1989, 12, 12, 12, 12).toInstant(ZoneOffset.UTC);

    @Autowired
    TaxRuleService taxRuleService;

    @Autowired
    TaxConfigRepository taxConfigRepository;

    @MockBean
    Clock clock;

    @BeforeEach
    void setup() {
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(_1989_12_12);
    }

    @Test
    void shouldCreateTaxConfigWithRule() {
        //given
        String countryCode = "country-code";
        TaxRule linearTaxRule = linearTaxRule("tax-code", 2, 3);

        //when
        taxRuleService.createTaxConfigWithRule(countryCode, linearTaxRule);

        //then
        assertEquals(1, taxRuleService.findAllConfigs().size());
        assertEquals(1, taxRuleService.findRules(countryCode).size());
        TaxConfig taxConfig = taxRuleService.findAllConfigs().get(0);

        assertNull(taxConfig.getDescription());
        assertNull(taxConfig.getCountryReason());
        assertEquals(countryCode, taxConfig.getCountryCode());
        assertEquals(_1989_12_12, taxConfig.getLastModifiedDate());
        assertEquals(1, taxConfig.getCurrentRulesCount());
        assertEquals(10, taxConfig.getMaxRulesCount());
        assertEquals(1, taxConfig.getTaxRules().size());

        TaxRule taxRule = taxRuleService.findRules(countryCode).get(0);

        assertEquals("tax-code", taxRule.getTaxCode());
        assertTrue(taxRule.isLinear());
        assertEquals(2, taxRule.getaFactor());
        assertEquals(3, taxRule.getbFactor());
    }

    @Test
    void shouldCreateTaxConfigWithRuleWithMaxCount() {
        //given
        String countryCode = "country-code";
        TaxRule linearTaxRule = linearTaxRule("tax-code", 2, 3);
        int maxRulesCount = 12;

        //when
        taxRuleService.createTaxConfigWithRule(countryCode, maxRulesCount, linearTaxRule);

        //then
        assertEquals(1, taxRuleService.findAllConfigs().size());
        assertEquals(1, taxRuleService.findRules(countryCode).size());
        TaxConfig taxConfig = taxRuleService.findAllConfigs().get(0);

        assertNull(taxConfig.getDescription());
        assertNull(taxConfig.getCountryReason());
        assertEquals(countryCode, taxConfig.getCountryCode());
        assertNotNull(taxConfig.getLastModifiedDate());
        assertEquals(1, taxConfig.getCurrentRulesCount());
        assertEquals(12, taxConfig.getMaxRulesCount());
        assertEquals(1, taxConfig.getTaxRules().size());

        TaxRule taxRule = taxRuleService.findRules(countryCode).get(0);

        assertEquals("tax-code", taxRule.getTaxCode());
        assertTrue(taxRule.isLinear());
        assertEquals(2, taxRule.getaFactor());
        assertEquals(3, taxRule.getbFactor());
    }

    @Test
    void shouldFailToCreateTaxConfigWithInvalidCountryCode() {
        //expect
        TaxRule linearTaxRule = linearTaxRule("tax-code", 2, 3);

        assertThrows(IllegalStateException.class, () -> taxRuleService.createTaxConfigWithRule(null, linearTaxRule));
        assertThrows(IllegalStateException.class, () -> taxRuleService.createTaxConfigWithRule("", linearTaxRule));
        assertThrows(IllegalStateException.class, () -> taxRuleService.createTaxConfigWithRule("l", linearTaxRule));
        assertThrows(IllegalStateException.class, () -> taxRuleService.createTaxConfigWithRule(null,12, linearTaxRule));
        assertThrows(IllegalStateException.class, () -> taxRuleService.createTaxConfigWithRule("",12, linearTaxRule));
        assertThrows(IllegalStateException.class, () -> taxRuleService.createTaxConfigWithRule("l",12, linearTaxRule));
    }

    @Test
    void shouldFailToAddTaxRuleToCountry() {
        //expect
        assertThrows(IllegalStateException.class, () -> taxRuleService.addTaxRuleToCountry(null, 2, 3, "tax"));
        assertThrows(IllegalStateException.class, () -> taxRuleService.addTaxRuleToCountry("", 2, 3, "tax"));
        assertThrows(IllegalStateException.class, () -> taxRuleService.addTaxRuleToCountry("l", 2, 3, "tax"));
        assertThrows(IllegalStateException.class, () -> taxRuleService.addTaxRuleToCountry("code", 0, 3, "tax"));

        assertThrows(IllegalStateException.class, () -> taxRuleService.addTaxRuleToCountry(null, 2, 3, 4, "tax"));
        assertThrows(IllegalStateException.class, () -> taxRuleService.addTaxRuleToCountry("", 2, 3, 4, "tax"));
        assertThrows(IllegalStateException.class, () -> taxRuleService.addTaxRuleToCountry("l", 2, 3,4,  "tax"));
        assertThrows(IllegalStateException.class, () -> taxRuleService.addTaxRuleToCountry("code", 0, 3, 4, "tax"));
    }

    @Test
    void shouldFailToAddTaxRuleToCountryWhenTooManyRules() {
        //given
        String countryCode = "country-code";
        TaxRule linearTaxRule = linearTaxRule("B1", 5, 6);
        taxRuleService.createTaxConfigWithRule(countryCode, 1, linearTaxRule);

        //expect
        assertThrows(IllegalStateException.class, () -> taxRuleService.addTaxRuleToCountry("code", 0, 3, 4, "tax"));
    }

    @Test
    void shouldAddLinearTaxRuleToNewlyCreatedConfig() {
        //given
        String countryCode = "country-code";
        int aFactor = 3;
        int bFactor = 2;
        String taxCode = "tax-code";

        //when
        taxRuleService.addTaxRuleToCountry(countryCode, aFactor, bFactor, taxCode);

        //then
        TaxConfig taxConfig = taxConfigRepository.findByCountryCode(countryCode);
        assertNull(taxConfig.getDescription());
        assertNull(taxConfig.getCountryReason());
        assertEquals(countryCode, taxConfig.getCountryCode());
        assertEquals(_1989_12_12, taxConfig.getLastModifiedDate());
        assertEquals(1, taxConfig.getCurrentRulesCount());
        assertEquals(10, taxConfig.getMaxRulesCount());
        assertEquals(1, taxConfig.getTaxRules().size());

        TaxRule taxRule = taxConfig.getTaxRules().get(0);

        assertEquals("A. 899. 1989tax-code", taxRule.getTaxCode());
        assertTrue(taxRule.isLinear());
        assertEquals(3, taxRule.getaFactor());
        assertEquals(2, taxRule.getbFactor());
    }

    @Test
    void shouldAddLinearTaxRuleToExistingConfig() {
        //given
        String countryCode = "country-code";
        int aFactor = 3;
        int bFactor = 2;
        String taxCode = "tax-code";

        //and tax rule exists
        TaxRule linearTaxRule = linearTaxRule("B1", 5, 6);
        taxRuleService.createTaxConfigWithRule(countryCode, linearTaxRule);

        //when
        taxRuleService.addTaxRuleToCountry(countryCode, aFactor, bFactor, taxCode);

        //then
        TaxConfig taxConfig = taxConfigRepository.findByCountryCode(countryCode);
        assertNull(taxConfig.getDescription());
        assertNull(taxConfig.getCountryReason());
        assertEquals(countryCode, taxConfig.getCountryCode());
        assertEquals(_1989_12_12, taxConfig.getLastModifiedDate());
        assertEquals(2, taxConfig.getCurrentRulesCount());
        assertEquals(10, taxConfig.getMaxRulesCount());
        assertEquals(2, taxConfig.getTaxRules().size());

        TaxRule taxRule = taxConfig.getTaxRules().get(0);
        assertEquals("B1", taxRule.getTaxCode());
        assertTrue(taxRule.isLinear());
        assertEquals(5, taxRule.getaFactor());
        assertEquals(6, taxRule.getbFactor());

        TaxRule newRule = taxConfig.getTaxRules().get(1);
        assertEquals("A. 899. 1989tax-code", newRule.getTaxCode());
        assertTrue(newRule.isLinear());
        assertEquals(3, newRule.getaFactor());
        assertEquals(2, newRule.getbFactor());
    }

    @Test
    void shouldAddSquareTaxRuleToNewlyCreatedConfig() {
        //given
        String countryCode = "country-code";
        int aFactor = 2;
        int bFactor = 3;
        int cFactor = 4;
        String taxCode = "tax-code";

        //when
        try {
            taxRuleService.addTaxRuleToCountry(countryCode, aFactor, bFactor, cFactor, taxCode);
        } catch (NullPointerException e) {
            System.out.println(e.getMessage());
        }

        //then
        TaxConfig taxConfig = taxConfigRepository.findByCountryCode(countryCode);
        assertNull(taxConfig.getDescription());
        assertNull(taxConfig.getCountryReason());
        assertEquals(countryCode, taxConfig.getCountryCode());
        assertEquals(_1989_12_12, taxConfig.getLastModifiedDate());
        assertEquals(1, taxConfig.getCurrentRulesCount());
        assertEquals(10, taxConfig.getMaxRulesCount());
        assertEquals(1, taxConfig.getTaxRules().size());

        TaxRule taxRule = taxConfig.getTaxRules().get(0);

        assertEquals("A. 899. 1989tax-code", taxRule.getTaxCode());
        assertTrue(taxRule.isSquare());
        assertEquals(2, taxRule.getaSquareFactor());
        assertEquals(3, taxRule.getbSquareFactor());
        assertEquals(4, taxRule.getcSuqreFactor());
    }

    @Test
    void shouldAddSquareTaxRuleToExistingConfig() {
        //given
        String countryCode = "country-code";
        int aFactor = 2;
        int bFactor = 3;
        int cFactor = 4;
        String taxCode = "tax-code";

        //and tax rule exists
        TaxRule linearTaxRule = linearTaxRule("B1", 5, 6);
        taxRuleService.createTaxConfigWithRule(countryCode, linearTaxRule);

        //when
        taxRuleService.addTaxRuleToCountry(countryCode, aFactor, bFactor, cFactor, taxCode);

        //then
        TaxConfig taxConfig = taxConfigRepository.findByCountryCode(countryCode);
        assertNull(taxConfig.getDescription());
        assertNull(taxConfig.getCountryReason());
        assertEquals(countryCode, taxConfig.getCountryCode());
        assertEquals(_1989_12_12, taxConfig.getLastModifiedDate());
        assertEquals(2, taxConfig.getCurrentRulesCount());
        assertEquals(10, taxConfig.getMaxRulesCount());
        assertEquals(2, taxConfig.getTaxRules().size());

        TaxRule taxRule = taxConfig.getTaxRules().get(0);
        assertEquals("B1", taxRule.getTaxCode());
        assertTrue(taxRule.isLinear());
        assertEquals(5, taxRule.getaFactor());
        assertEquals(6, taxRule.getbFactor());

        TaxRule newRule = taxConfig.getTaxRules().get(1);
        assertEquals("A. 899. 1989tax-code", newRule.getTaxCode());
        assertTrue(newRule.isSquare());
        assertEquals(2, newRule.getaSquareFactor());
        assertEquals(3, newRule.getbSquareFactor());
        assertEquals(4, newRule.getcSuqreFactor());
    }

    @Test
    void shouldRemoveRule() {
        //given
        String countryCode = "pl";
        TaxRule linearTaxRule = linearTaxRule("B1", 5, 6);
        TaxConfig taxConfig = taxRuleService.createTaxConfigWithRule(countryCode, linearTaxRule);
        taxRuleService.addTaxRuleToCountry(countryCode, 2, 3, "B2");

        //and
        Instant expectedDeletionDate = Instant.now();
        when(clock.instant()).thenReturn(expectedDeletionDate);

        //when
        taxRuleService.deleteRule(taxConfig.getTaxRules().get(1).getId(), taxConfig.getId());

        //then
        TaxConfig taxConfigResult = taxConfigRepository.findByCountryCode(countryCode);
        assertEquals(expectedDeletionDate,  taxConfigResult.getLastModifiedDate());
        assertEquals(1, taxConfigResult.getCurrentRulesCount());
        assertEquals(1, taxConfig.getTaxRules().size());

        TaxRule taxRuleResult = taxConfig.getTaxRules().get(0);
        assertEquals("B1", taxRuleResult.getTaxCode());
        assertTrue(taxRuleResult.isLinear());
        assertEquals(5, taxRuleResult.getaFactor());
        assertEquals(6, taxRuleResult.getbFactor());
    }

    @Test
    void shouldNotRemoveRule() {
        //given
        String countryCode = "pl";
        TaxRule linearTaxRule1 = linearTaxRule("B1", 5, 6);
        TaxRule linearTaxRule2 = linearTaxRule("B2", 5, 6);
        TaxConfig taxConfig1 = taxRuleService.createTaxConfigWithRule(countryCode, linearTaxRule1);
        TaxConfig taxConfig2 = taxRuleService.createTaxConfigWithRule(countryCode, linearTaxRule2);

        //when
        taxRuleService.deleteRule(taxConfig1.getTaxRules().get(0).getId(), taxConfig2.getId());

        //then
        assertEquals(1, taxConfig1.getTaxRules().size());
        assertEquals(1, taxConfig2.getTaxRules().size());
    }

    @Test
    void shouldFailToRemoveRule() {
        //given
        String countryCode = "pl";
        TaxRule linearTaxRule = linearTaxRule("B1", 5, 6);
        TaxConfig taxConfig = taxRuleService.createTaxConfigWithRule(countryCode, linearTaxRule);

        //expect
        assertThrows(IllegalStateException.class, () -> taxRuleService.deleteRule(taxConfig.getTaxRules().get(0).getId(), taxConfig.getId()));
    }

    private TaxRule linearTaxRule(String taxCode, int aFactor, int bFactor) {
        TaxRule taxRule = new TaxRule();
        taxRule.setTaxCode(taxCode);
        taxRule.setLinear(true);
        taxRule.setaFactor(aFactor);
        taxRule.setbFactor(bFactor);
        return taxRule;
    }

    private TaxRule squareTaxRule(String taxCode, int aFactor, int bFactor, int cFactor) {
        TaxRule taxRule = new TaxRule();
        taxRule.setTaxCode(taxCode);
        taxRule.setLinear(true);
        taxRule.setaSquareFactor(aFactor);
        taxRule.setbSquareFactor(bFactor);
        taxRule.setcSuqreFactor(cFactor);
        return taxRule;
    }
}