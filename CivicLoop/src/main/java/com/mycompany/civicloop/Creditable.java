package civicloop.model;

/**
 * Anything that can be exchanged for TimeCredit (an Item or a Service)
 * implements this interface. Each type decides its own credit rate,
 * which is how the project demonstrates POLYMORPHISM: the same method
 * call (getCreditRate()) behaves differently depending on the actual
 * object (Item vs Service) it is called on.
 */
public interface Creditable {
    // How many TimeCredits are earned per hour for this kind of offer.
    double getCreditRate();

    // A short label used in the GUI and in transaction records.
    String getOfferType();
}
