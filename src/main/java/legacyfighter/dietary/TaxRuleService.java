package legacyfighter.dietary;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.List;

@Service
public class TaxRuleService {

    @Autowired
    private TaxRuleRepository taxRuleRepository;

    @Autowired
    private TaxConfigRepository taxConfigRepository;

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private Clock clock;

    @Transactional
    public void addTaxRuleToCountry(String countryCode, int aFactor, int bFactor, String taxCode) {
        CountryCode codeVO = CountryCode.from(countryCode);

        if (aFactor == 0) {
            throw new IllegalStateException("Invalid aFactor");

        }
        TaxRule taxRule = new TaxRule();
        taxRule.setaFactor(aFactor);
        taxRule.setbFactor(bFactor);
        taxRule.setLinear(true);
        int year = Year.now(clock).getValue();
        taxRule.setTaxCode("A. 899. " + year + taxCode);
        TaxConfig taxConfig = taxConfigRepository.findByCountryCode(codeVO.getCode());
        if (taxConfig == null) {
            taxConfig = createTaxConfigWithRule(codeVO.getCode(), taxRule);
            return;
        }

        taxConfig.addTaxRule(taxRule, Instant.now(clock));

        List<Order> byOrderState = orderRepository.findByOrderState(Order.OrderState.Initial);

        byOrderState.forEach(order -> {
            if (order.getCustomerOrderGroup().getCustomer().getType().equals(Customer.Type.Person)) {
                order.getTaxRules().add(taxRule);
                orderRepository.save(order);
            }

        });
    }

    @Transactional
    public TaxConfig createTaxConfigWithRule(String countryCode, TaxRule taxRule) {
        return createTaxConfigWithRule(countryCode, 10, taxRule);
    }

    @Transactional
    public TaxConfig createTaxConfigWithRule(String countryCode, int maxRulesCount, TaxRule taxRule) {
        TaxConfig taxConfig = TaxConfig.from(CountryCode.from(countryCode), maxRulesCount);
        taxConfig.addTaxRule(taxRule, Instant.now(clock));
        taxConfigRepository.save(taxConfig);
        return taxConfig;
    }

    @Transactional
    public void addTaxRuleToCountry(String countryCode, int aFactor, int bFactor, int cFactor, String taxCode) {
        if (aFactor == 0) {
            throw new IllegalStateException("Invalid aFactor");
        }

        CountryCode codeVO = CountryCode.from(countryCode);

        TaxRule taxRule = new TaxRule();
        taxRule.setaSquareFactor(aFactor);
        taxRule.setbSquareFactor(bFactor);
        taxRule.setcSuqreFactor(cFactor);
        taxRule.setSquare(true);
        int year = Year.now(clock).getValue();
        taxRule.setTaxCode("A. 899. " + year + taxCode);
        TaxConfig taxConfig = taxConfigRepository.findByCountryCode(codeVO.getCode());
        if (taxConfig == null) {
            createTaxConfigWithRule(codeVO.getCode(), taxRule);
        }
        taxConfig.addTaxRule(taxRule, Instant.now(clock));
    }

    @Transactional
    public void deleteRule(Long taxRuleId, Long configId) {
        TaxRule taxRule = taxRuleRepository.getOne(taxRuleId);
        TaxConfig taxConfig = taxConfigRepository.getOne(configId);
        taxConfig.removeTaxRule(taxRule, Instant.now(clock));
    }

    @Transactional
    public List<TaxRule> findRules(String countryCode) {
        return taxConfigRepository.findByCountryCode(countryCode).getTaxRules();
    }

    @Transactional
    public int rulesCount(String countryCode) {
        return taxConfigRepository.findByCountryCode(countryCode).getCurrentRulesCount();
    }

    public List<TaxConfig> findAllConfigs() {
        return taxConfigRepository.findAll();
    }
}
