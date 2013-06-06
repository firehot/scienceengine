using System;
using System.Linq;
using MonoTouch.StoreKit;
using MonoTouch.Foundation;
using MonoTouch.UIKit;

namespace scienceengineios {
  internal class PaymentObserver : SKPaymentTransactionObserver {
    private InAppPurchaseManager inAppPurchaseManager;

    public PaymentObserver(InAppPurchaseManager manager) {
      inAppPurchaseManager = manager;
    }

    // called when the transaction status is updated
    public override void UpdatedTransactions (SKPaymentQueue queue, SKPaymentTransaction[] transactions) {
      Console.WriteLine ("UpdatedTransactions");
      foreach (SKPaymentTransaction transaction in transactions) {
          switch (transaction.TransactionState) {
              case SKPaymentTransactionState.Purchased:
                 inAppPurchaseManager.CompleteTransaction(transaction);
                  break;
              case SKPaymentTransactionState.Failed:
                 inAppPurchaseManager.FailedTransaction(transaction);
                  break;
              case SKPaymentTransactionState.Restored:
                  inAppPurchaseManager.RestoreTransaction(transaction);
                  break;
              default:
                  break;
          }
      }
    }

    public override void PaymentQueueRestoreCompletedTransactionsFinished (SKPaymentQueue queue) {
      // Restore succeeded
      Console.WriteLine(" ** RESTORE PaymentQueueRestoreCompletedTransactionsFinished ");
    }
    
    public override void RestoreCompletedTransactionsFailedWithError (SKPaymentQueue queue, NSError error) {
      // Restore failed somewhere...
      Console.WriteLine(" ** RESTORE RestoreCompletedTransactionsFailedWithError " + error.LocalizedDescription);
    }
  }
}
