package legacyfighter.dietary;

import javax.persistence.*;
import java.time.Year;
import java.util.Objects;

@Entity
public class TaxRule {

    public TaxRule() {

    }

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String taxCode;

    private boolean isLinear;
    private Integer aFactor;
    private Integer bFactor;

    private boolean isSquare;
    private Integer aSquareFactor;
    private Integer bSquareFactor;
    private Integer cSquareFactor;

    @ManyToOne
    private TaxConfig taxConfig;

    public static TaxRule linearRule(int a, int b, String taxCode, Year year) {
        if (a == 0) {
            throw new IllegalStateException("Invalid aFactor");
        }

        TaxRule rule = new TaxRule();
        rule.isLinear = true;
        rule.aFactor = a;
        rule.bFactor = b;
        rule.taxCode = "A. 899. " + year.getValue() + taxCode;
        return rule;
    }

    public static TaxRule squareRule(int a, int b, int c, String taxCode, Year year) {
        if (a == 0) {
            throw new IllegalStateException("Invalid aFactor");
        }

        TaxRule rule = new TaxRule();
        rule.isSquare = true;
        rule.aSquareFactor = a;
        rule.bSquareFactor = b;
        rule.cSquareFactor = c;
        rule.taxCode = "A. 899. " + year.getValue() + taxCode;
        return rule;
    }

    public boolean isLinear() {
        return isLinear;
    }

    public Integer getaFactor() {
        return aFactor;
    }

    public Integer getbFactor() {
        return bFactor;
    }

    public boolean isSquare() {
        return isSquare;
    }

    public Integer getaSquareFactor() {
        return aSquareFactor;
    }

    public Integer getbSquareFactor() {
        return bSquareFactor;
    }

    public Integer getcSuqreFactor() {
        return cSquareFactor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TaxRule)) {
            return false;
        }
        TaxRule that = (TaxRule) o;
        return taxCode.equals(that.getTaxCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(taxCode);
    }


    public String getTaxCode() {
        return taxCode;
    }

    public Long getId() {
        return id;
    }
}
