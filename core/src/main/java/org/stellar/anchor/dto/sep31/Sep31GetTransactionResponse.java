package org.stellar.anchor.dto.sep31;

import com.google.gson.annotations.SerializedName;
import java.time.Instant;
import java.util.List;
import lombok.Data;
import org.stellar.anchor.asset.AssetInfo;
import org.stellar.anchor.model.Sep31Transaction;

@Data
public class Sep31GetTransactionResponse {
  TransactionResponse transaction;

  @Data
  public static class TransactionResponse {
    String id;
    String status;

    @SerializedName("status_eta")
    Long statusEta;

    @SerializedName("amount_in")
    String amountIn;

    @SerializedName("amount_in_asset")
    String amountInAsset;

    @SerializedName("amount_out")
    String amountOut;

    @SerializedName("amount_out_asset")
    String amountOutAsset;

    @SerializedName("amount_fee")
    String amountFee;

    @SerializedName("amount_fee_asset")
    String amountFeeAsset;

    @SerializedName("stellar_account_id")
    String stellarAccountId;

    @SerializedName("stellar_memo_type")
    String stellarMemoType;

    @SerializedName("started_at")
    Instant startedAt;

    @SerializedName("completed_at")
    Instant completedAt;

    @SerializedName("stellar_transaction_id")
    String stellarTransactionId;

    @SerializedName("external_transaction_id")
    String externalTransactionId;

    Boolean refunded;
    Refunds refunds;

    @SerializedName("required_info_message")
    String requiredInfoMessage;

    @SerializedName("required_info_updates")
    AssetInfo.Sep31TxnFields requiredInfoUpdates;
  }

  @Data
  public static class Refunds {
    @SerializedName("amount_refunded")
    String amountRefunded;

    @SerializedName("amount_fee")
    String amountFee;

    List<Sep31Transaction.RefundPayment> payments;
  }

  @Data
  public static class JdbcRefundPayment implements Sep31Transaction.RefundPayment {
    String Id;
    String amount;
    String fee;
  }
}
