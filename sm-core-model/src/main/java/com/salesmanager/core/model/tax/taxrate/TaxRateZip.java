package com.salesmanager.core.model.tax.taxrate;

import com.salesmanager.core.model.common.audit.AuditListener;
import com.salesmanager.core.model.common.audit.AuditSection;
import com.salesmanager.core.model.common.audit.Auditable;
import com.salesmanager.core.model.generic.SalesManagerEntity;
import com.salesmanager.core.model.merchant.MerchantStore;
import com.salesmanager.core.model.reference.country.Country;
import com.salesmanager.core.model.reference.zone.Zone;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@EntityListeners(value = AuditListener.class)
@Table(name = "TAX_RATE_ZIP", uniqueConstraints = {
        @UniqueConstraint(columnNames = {
                "ZIP_CODE",
                "MERCHANT_ID"
        })
}
)
public class TaxRateZip extends SalesManagerEntity<Long, TaxRateZip> implements Auditable {
    private static final long serialVersionUID = 3356827741612925066L;

    @Id
    @Column(name = "TAX_RATE_ZIP_ID")
    @TableGenerator(name = "TABLE_GEN", table = "SM_SEQUENCER", pkColumnName = "SEQ_NAME", valueColumnName = "SEQ_COUNT", pkColumnValue = "TAX_RATE_ZIP_ID_NEXT_VALUE")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "TABLE_GEN")
    private Long id;

    @Embedded
    private AuditSection auditSection = new AuditSection();

    @NotEmpty
    @Column(name = "STATE")
    private String state;

    @NotEmpty
    @Column(name = "ZIP_CODE")
    private String code;

    @Column(name = "EST_RATE", nullable = false, precision = 7, scale = 4)
    private BigDecimal estRate;

    @ManyToOne
    @JoinColumn(name = "TAX_RATE_ID", nullable = false)
    private TaxRate taxRate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MERCHANT_ID", nullable = false)
    private MerchantStore merchantStore;

    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Country.class)
    @JoinColumn(name = "COUNTRY_ID", nullable = false, updatable = true)
    private Country country;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ZONE_ID", nullable = true, updatable = true)
    private Zone zone;

    @Column(name = "STORE_STATE_PROV", length = 100)
    private String stateProvince;

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    private TaxRateZip parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<TaxRateZip> taxRatesZip = new ArrayList<TaxRateZip>();


    @Override
    public AuditSection getAuditSection() {
        return null;
    }

    @Override
    public void setAuditSection(AuditSection audit) {

    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {

    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public BigDecimal getEstRate() {
        return estRate;
    }

    public void setEstRate(BigDecimal estRate) {
        this.estRate = estRate;
    }

    public TaxRate getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(TaxRate taxRate) {
        this.taxRate = taxRate;
    }

    public MerchantStore getMerchantStore() {
        return merchantStore;
    }

    public void setMerchantStore(MerchantStore merchantStore) {
        this.merchantStore = merchantStore;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Zone getZone() {
        return zone;
    }

    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public String getStateProvince() {
        return stateProvince;
    }

    public void setStateProvince(String stateProvince) {
        this.stateProvince = stateProvince;
    }

    public TaxRateZip getParent() {
        return parent;
    }

    public void setParent(TaxRateZip parent) {
        this.parent = parent;
    }

    public List<TaxRateZip> getTaxRatesZip() {
        return taxRatesZip;
    }

    public void setTaxRatesZip(List<TaxRateZip> taxRatesZip) {
        this.taxRatesZip = taxRatesZip;
    }
}