package com.example.banktransaction.vo;

import com.example.banktransaction.serializer.JaxbDateSerializer;
import lombok.*;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.math.BigDecimal;
import java.util.Date;

@XmlRootElement(name = "transaction")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Transaction {

    private Long transactionId;
    private Long accountId;
    private String description;
    private BigDecimal credit;
    private BigDecimal debit;
    private Date timestamp;

    @XmlJavaTypeAdapter(JaxbDateSerializer.class)
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public BigDecimal getTransactionAmount() {
        if(credit != null) {
            if(debit != null) {
                return credit.add(debit);
            } else {
                return credit;
            }
        }
        else if(debit != null) {
            return debit;
        }
        else {
            return new BigDecimal(0);
        }
    }
}
