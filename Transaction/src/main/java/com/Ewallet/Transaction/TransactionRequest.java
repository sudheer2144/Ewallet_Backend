package com.Ewallet.Transaction;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequest {
    private int amount;
    private String toUser;
    private String fromUser;
    private String purpose;
}
