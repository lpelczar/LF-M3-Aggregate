package legacyfighter.dietary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
class TaxConfigQueryIntegrationTest {

    static Instant _1989_12_12 = LocalDateTime.of(1989, 12, 12, 12, 12).toInstant(ZoneOffset.UTC);

    @Autowired
    TaxConfigController taxConfigController;

    @Autowired
    TaxRuleService taxRuleService;

    @MockBean
    Clock clock;

    @BeforeEach
    void setup() {
        when(clock.getZone()).thenReturn(ZoneId.systemDefault());
        when(clock.instant()).thenReturn(_1989_12_12);
    }

    @Test
    void shouldReturnTaxConfigs() {
        //given
        taxRuleService.createTaxConfigWithRule("pl", TaxRule.linearRule(5, 6, "pllinear", Year.of(1989)));
        taxRuleService.createTaxConfigWithRule("en", TaxRule.squareRule(5, 6, 8, "ensquare", Year.of(1989)));
        taxRuleService.createTaxConfigWithRule("mex", TaxRule.squareRule(7, 8, 9, "mexsquare", Year.of(1989)));
        taxRuleService.addTaxRuleToCountry("pl", 2, 3, 4,"plsquare");

        //when
        Map<String, List<TaxRule>> result = taxConfigController.taxConfigs();

        //then
        assertEquals(3, result.size());
        assertEquals(2, result.get("pl").size());
        assertEquals(1, result.get("en").size());
        assertEquals(1, result.get("mex").size());

        TaxRule plRule1 = result.get("pl").get(0);
        assertEquals("A. 899. 1989pllinear", plRule1.getTaxCode());
        assertTrue(plRule1.isLinear());
        assertEquals(5, plRule1.getaFactor());
        assertEquals(6, plRule1.getbFactor());

        TaxRule plRule2 = result.get("pl").get(1);
        assertEquals("A. 899. 1989plsquare", plRule2.getTaxCode());
        assertTrue(plRule2.isSquare());
        assertEquals(2, plRule2.getaSquareFactor());
        assertEquals(3, plRule2.getbSquareFactor());
        assertEquals(4, plRule2.getcSquareFactor());

        TaxRule enRule = result.get("en").get(0);
        assertEquals("A. 899. 1989ensquare", enRule.getTaxCode());
        assertTrue(enRule.isSquare());
        assertEquals(5, enRule.getaSquareFactor());
        assertEquals(6, enRule.getbSquareFactor());
        assertEquals(8, enRule.getcSquareFactor());

        TaxRule mexRule = result.get("mex").get(0);
        assertEquals("A. 899. 1989mexsquare", mexRule.getTaxCode());
        assertTrue(mexRule.isSquare());
        assertEquals(7, mexRule.getaSquareFactor());
        assertEquals(8, mexRule.getbSquareFactor());
        assertEquals(9, mexRule.getcSquareFactor());
    }

}