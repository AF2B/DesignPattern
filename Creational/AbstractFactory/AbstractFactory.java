/*
 * Abstract Factory — Creational Pattern (Java 25)
 *
 * A logistics company ships parcels by air, land and sea. Each transport mode
 * needs a whole family of components that only make sense together: a carrier,
 * a packaging and a tracking channel. A cargo plane must never be paired with a
 * shipping container, nor a truck with a ship's AIS transponder.
 *
 * The abstract factory (LogisticsFactory) creates the whole family at once, so
 * the client code (ship) depends only on interfaces and can never mix products
 * from different families.
 *
 * Run with: java AbstractFactory.java
 */

static final BigDecimal AIR_COST_PER_KM = new BigDecimal("5.00");
static final BigDecimal LAND_COST_PER_KM = new BigDecimal("2.00");
static final BigDecimal SEA_COST_PER_KM = new BigDecimal("1.00");

static final int PRESSURIZED_CRATE_MAX_KG = 500;
static final int PALLET_MAX_KG = 1_000;
static final int SHIPPING_CONTAINER_MAX_KG = 28_000;

// ── Abstract products ───────────────────────────────────────────────────────

/** Vehicle that moves the parcel. */
sealed interface Carrier permits CargoPlane, Truck, CargoShip {

    /**
     * Human-readable carrier name.
     *
     * @return the carrier name
     */
    String name();

    /**
     * Price charged for each kilometer travelled.
     *
     * @return the cost per kilometer
     */
    BigDecimal costPerKm();
}

/** Container that protects the parcel during transport. */
sealed interface Packaging permits PressurizedCrate, Pallet, ShippingContainer {

    /**
     * Human-readable packaging name.
     *
     * @return the packaging name
     */
    String name();

    /**
     * Heaviest load this packaging accepts.
     *
     * @return the maximum weight in kilograms
     */
    int maxWeightKg();
}

/** Channel used to follow the parcel while it travels. */
sealed interface Tracker permits FlightRadar, GpsTracker, AisTransponder {

    /**
     * Describes how the parcel will be tracked on its way to the destination.
     *
     * @param destination where the parcel is going
     * @return the tracking description
     */
    String trackingChannel(String destination);
}

// ── Concrete products: air family ───────────────────────────────────────────

record CargoPlane() implements Carrier {

    @Override
    public String name() {
        return "Cargo Plane";
    }

    @Override
    public BigDecimal costPerKm() {
        return AIR_COST_PER_KM;
    }
}

record PressurizedCrate() implements Packaging {

    @Override
    public String name() {
        return "Pressurized Crate";
    }

    @Override
    public int maxWeightKg() {
        return PRESSURIZED_CRATE_MAX_KG;
    }
}

record FlightRadar() implements Tracker {

    @Override
    public String trackingChannel(String destination) {
        return "Flight radar to " + destination;
    }
}

// ── Concrete products: land family ──────────────────────────────────────────

record Truck() implements Carrier {

    @Override
    public String name() {
        return "Truck";
    }

    @Override
    public BigDecimal costPerKm() {
        return LAND_COST_PER_KM;
    }
}

record Pallet() implements Packaging {

    @Override
    public String name() {
        return "Pallet";
    }

    @Override
    public int maxWeightKg() {
        return PALLET_MAX_KG;
    }
}

record GpsTracker() implements Tracker {

    @Override
    public String trackingChannel(String destination) {
        return "GPS on the road to " + destination;
    }
}

// ── Concrete products: sea family ───────────────────────────────────────────

record CargoShip() implements Carrier {

    @Override
    public String name() {
        return "Cargo Ship";
    }

    @Override
    public BigDecimal costPerKm() {
        return SEA_COST_PER_KM;
    }
}

record ShippingContainer() implements Packaging {

    @Override
    public String name() {
        return "Shipping Container";
    }

    @Override
    public int maxWeightKg() {
        return SHIPPING_CONTAINER_MAX_KG;
    }
}

record AisTransponder() implements Tracker {

    @Override
    public String trackingChannel(String destination) {
        return "AIS transponder to " + destination;
    }
}

// ── Abstract factory and concrete factories ─────────────────────────────────

/** Creates a family of logistics components that are compatible by design. */
sealed interface LogisticsFactory permits AirLogistics, LandLogistics, SeaLogistics {

    /**
     * Creates the carrier of this family.
     *
     * @return a new carrier
     */
    Carrier createCarrier();

    /**
     * Creates the packaging of this family.
     *
     * @return a new packaging
     */
    Packaging createPackaging();

    /**
     * Creates the tracker of this family.
     *
     * @return a new tracker
     */
    Tracker createTracker();
}

record AirLogistics() implements LogisticsFactory {

    @Override
    public Carrier createCarrier() {
        return new CargoPlane();
    }

    @Override
    public Packaging createPackaging() {
        return new PressurizedCrate();
    }

    @Override
    public Tracker createTracker() {
        return new FlightRadar();
    }
}

record LandLogistics() implements LogisticsFactory {

    @Override
    public Carrier createCarrier() {
        return new Truck();
    }

    @Override
    public Packaging createPackaging() {
        return new Pallet();
    }

    @Override
    public Tracker createTracker() {
        return new GpsTracker();
    }
}

record SeaLogistics() implements LogisticsFactory {

    @Override
    public Carrier createCarrier() {
        return new CargoShip();
    }

    @Override
    public Packaging createPackaging() {
        return new ShippingContainer();
    }

    @Override
    public Tracker createTracker() {
        return new AisTransponder();
    }
}

// ── Factory selection ───────────────────────────────────────────────────────

/** Closed set of transport modes the company operates. */
enum TransportMode {
    AIR("air"),
    LAND("land"),
    SEA("sea");

    private final String label;

    TransportMode(String label) {
        this.label = label;
    }

    /**
     * Parses user input into a transport mode, ignoring case and spaces.
     *
     * @param label the raw transport mode, such as "air"
     * @return the matching mode, or empty when the mode is not operated
     */
    static Optional<TransportMode> fromLabel(String label) {
        return Arrays.stream(values())
                .filter(mode -> mode.label.equalsIgnoreCase(label.strip()))
                .findFirst();
    }
}

/**
 * Picks the concrete factory for a transport mode. The switch is exhaustive:
 * adding a new mode without a factory is a compile error.
 *
 * @param mode the transport mode
 * @return the factory that builds that mode's component family
 */
static LogisticsFactory factoryFor(TransportMode mode) {
    return switch (mode) {
        case AIR -> new AirLogistics();
        case LAND -> new LandLogistics();
        case SEA -> new SeaLogistics();
    };
}

// ── Client ──────────────────────────────────────────────────────────────────

/**
 * Something to be shipped.
 *
 * @param description what the parcel contains
 * @param weightKg the parcel weight in kilograms
 */
record Parcel(String description, int weightKg) {}

/** Outcome of a shipment request: an expected failure is a value, not an exception. */
sealed interface ShipmentResult permits Scheduled, Overweight {}

/**
 * The parcel was accepted.
 *
 * @param carrier the carrier name
 * @param packaging the packaging name
 * @param cost the total delivery cost
 * @param tracking how the parcel will be tracked
 */
record Scheduled(
        String carrier,
        String packaging,
        BigDecimal cost,
        String tracking) implements ShipmentResult {}

/**
 * The parcel is heavier than the family's packaging accepts.
 *
 * @param packaging the packaging name
 * @param weightKg the parcel weight in kilograms
 * @param maxWeightKg the packaging limit in kilograms
 */
record Overweight(
        String packaging,
        int weightKg,
        int maxWeightKg) implements ShipmentResult {}

/**
 * Ships a parcel using only the abstract factory and abstract products.
 *
 * @param factory the family of logistics components to use
 * @param parcel the parcel to ship
 * @param destination where the parcel is going
 * @param distanceKm the distance to the destination in kilometers
 * @return the scheduled shipment, or why it was rejected
 */
static ShipmentResult ship(
        LogisticsFactory factory,
        Parcel parcel,
        String destination,
        int distanceKm) {
    var carrier = factory.createCarrier();
    var packaging = factory.createPackaging();
    var tracker = factory.createTracker();

    if (parcel.weightKg() > packaging.maxWeightKg()) {
        return new Overweight(
                packaging.name(),
                parcel.weightKg(),
                packaging.maxWeightKg());
    }

    var cost = carrier.costPerKm().multiply(BigDecimal.valueOf(distanceKm));

    return new Scheduled(
            carrier.name(),
            packaging.name(),
            cost,
            tracker.trackingChannel(destination));
}

/**
 * Renders a shipment result as a single line.
 *
 * @param result the shipment result
 * @return the formatted line
 */
static String render(ShipmentResult result) {
    return switch (result) {
        case Scheduled(var carrier, var packaging, var cost, var tracking) ->
                "Scheduled: %s + %s, cost %s, tracked by %s"
                        .formatted(carrier, packaging, cost, tracking);
        case Overweight(var packaging, var weightKg, var maxWeightKg) ->
                "Rejected: %d kg exceeds the %s limit of %d kg"
                        .formatted(weightKg, packaging, maxWeightKg);
    };
}

/**
 * Parses the transport mode and ships the parcel with the matching family.
 *
 * @param modeLabel the raw transport mode, such as "air"
 * @param parcel the parcel to ship
 * @param destination where the parcel is going
 * @param distanceKm the distance to the destination in kilometers
 * @return the formatted outcome
 */
static String quote(
        String modeLabel,
        Parcel parcel,
        String destination,
        int distanceKm) {
    return TransportMode.fromLabel(modeLabel)
            .map(mode -> ship(factoryFor(mode), parcel, destination, distanceKm))
            .map(result -> render(result))
            .orElse("Unknown transport mode: " + modeLabel);
}

void main() {
    var boxOfParts = new Parcel("Box of Parts", 120);
    var heavyCargo = new Parcel("Heavy Cargo", 2_000);

    IO.println(quote("air", boxOfParts, "Pernambuco", 100));
    IO.println(quote("land", boxOfParts, "Rio Grande Do Sul", 50));
    IO.println(quote("air", heavyCargo, "Port of Santos", 200));
    IO.println(quote("sea", heavyCargo, "Port of Santos", 200));
    IO.println(quote("space", boxOfParts, "Moon", 384_400));
}
