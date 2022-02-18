package legacyfighter.dietary;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.*;
import java.util.stream.Collectors;

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
        TaxRule taxRule = TaxRule.linearRule(aFactor, bFactor, taxCode, Year.now(clock));

        TaxConfig taxConfig = taxConfigRepository.findByCountryCode(countryCode);
        if (taxConfig == null) {
            createTaxConfigWithRule(countryCode, taxRule);
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
        TaxConfig taxConfig = TaxConfig.from(countryCode, maxRulesCount);
        taxConfig.addTaxRule(taxRule, Instant.now(clock));
        taxConfigRepository.save(taxConfig);
        return taxConfig;
    }

    @Transactional
    public void addTaxRuleToCountry(String countryCode, int aFactor, int bFactor, int cFactor, String taxCode) {
        TaxRule taxRule = TaxRule.squareRule(aFactor, bFactor, cFactor, taxCode, Year.now(clock));

        TaxConfig taxConfig = taxConfigRepository.findByCountryCode(countryCode);
        if (taxConfig == null) {
            createTaxConfigWithRule(countryCode, taxRule);
            return;
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

    public Map<String, List<TaxRule>> findAllRules() {
        return taxConfigRepository.findAll().stream()
                .collect(Collectors.groupingBy(TaxConfig::getCountryCode,
                        Collectors.mapping(
                                TaxConfig::getTaxRules,
                                Collectors.collectingAndThen(Collectors.toList(),
                                        list -> list.stream()
                                                .flatMap(List::stream)
                                                .collect(Collectors.toList())))
                ));
    }
}
