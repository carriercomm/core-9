/*
 * Copyright (c) 2014 Vladislav Zablotsky
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.jprocessing.entities;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Properties;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import org.slf4j.LoggerFactory;

/**
 * Each payment holds info about adding funds (real money) to customers account.
 * Customer account receive funds through external payment systems. Here we are
 * recording transaction details for each payment system.
 * Successful payment have to create accounting debit record.
 *
 * @author rumatoest
 */
@Entity
@Table(name = "jp_payments",
    indexes = {
        @Index(columnList = "transaction_id"),
        @Index(columnList = "status"),
        @Index(columnList = "accounting_id")
    }
)
public class Payment implements JpEntity<Long> {

    private static final long serialVersionUID = 6491268568658870088L;

    /**
     * Payment transaction started and we are waiting for completion (callback from payment system).
     */
    public final int STATUS_OPEN = 0;

    /**
     * Transaction successfully completed.
     * Actually any positive value mean success.
     */
    public final int STATUS_SUCCESS = 1;

    /**
     * Transaction completed with error (basically unknown).
     * Any error is a negative value.
     */
    public final int STATUS_ERROR = -1;

    /**
     * Transaction closed over big timeout.
     */
    public final int STATUS_TIMEOUT = -10;

    /**
     * Fraud payment was detected
     */
    public final int STATUS_FRAUD = -20;

    @Id
    @Column(name = "id")
    private Long pk;

    @Override
    public Long getPk() {
        return pk;
    }

    @Override
    public void setPk(Long primaryKey) {
        this.pk = primaryKey;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "start_time", nullable = false)
    private Calendar startTime;

    /**
     * Return timestamp when transaction was initiated
     */
    public Calendar getStartTime() {
        return startTime;
    }

    /**
     * Set transactions start time.
     * This should be timestamp from remote payment system.
     */
    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "complete_time")
    private Calendar completeTime;

    /**
     * Return timestamp when transaction was completed.
     */
    public Calendar getCompleteTime() {
        return completeTime;
    }

    /**
     * Set timestamp when jprocessing complete transaction.
     */
    public void setCompleteTime(Calendar completeTime) {
        this.completeTime = completeTime;
    }

    @Column(name = "status")
    private int status = 0;

    /**
     * Return current transaction status.
     * See STATUS_* constants.
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set transaction status.
     * Note, that any non zero status mean that transaction have to be completed - complete time
     * should not be null.
     */
    public void setStatus(int status) {
        this.status = status;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accounting_id", nullable = true)
    private Accounting accountig;

    /**
     * Get associated accounting record
     */
    public Accounting getAccountig() {
        return accountig;
    }

    /**
     * Set associated accounting record.
     * This means that current payment transaction produced new record in accounting.
     */
    public void setAccountig(Accounting accountig) {
        this.accountig = accountig;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = true)
    private Invoice invoice;

    /**
     * Get invoice associated with current payment.
     * Means that payment made by linked invoice.
     */
    public Invoice getInvoice() {
        return invoice;
    }

    /**
     * Set invoice associated with current payment.
     * Means that payment will link only to provided invoice.
     */
    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    /**
     * Get amount of money related to this transaction
     */
    public BigDecimal getAmount() {
        return amount;
    }

    /**
     * Set amount of money related to this transaction
     */
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /**
     * Set amount of money related to this transaction
     */
    public void setAmount(double amount) {
        this.amount = BigDecimal.valueOf(amount);
    }

    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    /**
     * Get currency code for sum field
     */
    public String getCurrency() {
        return currency;
    }

    /**
     * Set currency code for sum field
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Column(name = "payment_processor", length = 64, nullable = false)
    private String paymentProcessor;

    /**
     * Return payment processor code.
     * Usually payment processor class name.
     */
    public String getPaymentProcessor() {
        return paymentProcessor;
    }

    /**
     * Set payment processor code for this transaction.
     * Usually payment processor class name.
     */
    public void setPaymentProcessor(String paymentProcessor) {
        this.paymentProcessor = paymentProcessor;
    }

    @Column(name = "transaction_id", length = 128, nullable = false, unique = true)
    private String transactionId;

    /**
     * Return transaction id (hash).
     * This have to be unique value.
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Set transaction id (hash).
     * This have to be unique value, and it should be form by payment processor.
     */
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    @Lob
    @Column(name = "properties", length = 2048)
    private String properties;

    /**
     * Cache properties values in object representation
     */
    @Transient
    private Properties propertiesCache;

    /**
     * Get additional properties associated with transaction.
     * Any changes on returned properties object will not be persisted.
     * Use setProperties to update entity values.
     */
    public Properties getPropertiesAsObj() {
        if (propertiesCache == null) {
            propertiesCache = new Properties();
            try {
                propertiesCache.load(new StringReader(String.valueOf(getProperties())));
            } catch (IOException ex) {
                LoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
            }
        }
        return propertiesCache;
    }

    /**
     * Get additional properties associated with transaction and compatible with java Properties.
     */
    public String getProperties() {
        return properties;
    }

    /**
     * Set additional properties associated with transaction and compatible with java Properties.
     */
    public void setProperties(String properties) {
        this.properties = properties;
    }

    /**
     * Set additional properties associated with transaction.
     */
    public void setProperties(Properties properties) {
        StringWriter sw = new StringWriter();

        try {
            properties.store(sw, getClass().getName() + " generated properties");
        } catch (IOException ex) {
            LoggerFactory.getLogger(getClass()).error(ex.getMessage(), ex);
        }

        setProperties(sw.getBuffer().toString());
        this.propertiesCache = properties;
    }

}