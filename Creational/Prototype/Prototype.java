/*
 * Prototype — Creational Pattern (Java 25)
 *
 * A logistics company keeps preconfigured shipment orders (express domestic,
 * fragile domestic, international electronics). Building one from scratch means
 * choosing the service, handling instructions and customs items every time.
 * Instead, each template is a prototype: new orders are copies of it that the
 * clerk only customizes with the recipient and destination.
 *
 * The registry clones prototypes through ShipmentOrder.copy() without knowing
 * their concrete class. Copies are deep: mutating a copy never leaks into the
 * prototype or into other copies. Cloneable/Object.clone() is deliberately
 * avoided in favor of copy constructors (Effective Java, 3rd ed., Item 13).
 *
 * Run with: java Prototype.java
 */

static final String UNASSIGNED = "<unassigned>";

static final String EXPRESS_SERVICE = "Express";
static final String STANDARD_SERVICE = "Standard";
static final String INTERNATIONAL_SERVICE = "International Priority";

static final String HANDLE_WITH_CARE = "Handle with care";
static final String KEEP_UPRIGHT = "Keep upright";
static final String SIGNATURE_REQUIRED = "Signature required";
static final String CONTAINS_LITHIUM_BATTERIES = "Contains lithium batteries";

static final String LAPTOP = "Laptop";
static final String SMARTPHONE = "Smartphone";

// ── Prototype hierarchy ─────────────────────────────────────────────────────

/** A shipment order that knows how to copy itself. */
abstract static sealed class ShipmentOrder permits DomesticOrder, InternationalOrder {

    private final String service;
    private final List<String> handlingInstructions;
    private String recipient = UNASSIGNED;
    private String destination = UNASSIGNED;

    /**
     * Creates a fresh order with no recipient assigned yet.
     *
     * @param service the shipping service name
     * @param handlingInstructions the instructions printed on the label
     */
    protected ShipmentOrder(String service, List<String> handlingInstructions) {
        this.service = service;
        this.handlingInstructions = new ArrayList<>(handlingInstructions);
    }

    /**
     * Copy constructor: duplicates every field, deep-copying mutable ones.
     *
     * @param source the order being copied
     */
    protected ShipmentOrder(ShipmentOrder source) {
        this.service = source.service;
        this.handlingInstructions = new ArrayList<>(source.handlingInstructions);
        this.recipient = source.recipient;
        this.destination = source.destination;
    }

    /**
     * Creates an independent copy of this order with the same concrete type.
     *
     * @return the copy
     */
    abstract ShipmentOrder copy();

    /**
     * Sets who receives the parcel and where.
     *
     * @param recipient the person or company receiving the parcel
     * @param destination the delivery address
     */
    void assignRecipient(String recipient, String destination) {
        this.recipient = recipient;
        this.destination = destination;
    }

    /**
     * Appends an instruction to the label.
     *
     * @param instruction the handling instruction
     */
    void addHandlingInstruction(String instruction) {
        handlingInstructions.add(instruction);
    }

    String service() {
        return service;
    }

    List<String> handlingInstructions() {
        return List.copyOf(handlingInstructions);
    }

    String recipient() {
        return recipient;
    }

    String destination() {
        return destination;
    }
}

/** Order delivered inside the country. */
static final class DomesticOrder extends ShipmentOrder {

    /**
     * Creates a domestic order template.
     *
     * @param service the shipping service name
     * @param handlingInstructions the instructions printed on the label
     */
    DomesticOrder(String service, List<String> handlingInstructions) {
        super(service, handlingInstructions);
    }

    private DomesticOrder(DomesticOrder source) {
        super(source);
    }

    @Override
    DomesticOrder copy() {
        return new DomesticOrder(this);
    }
}

/** Order that crosses a border and therefore carries a customs declaration. */
static final class InternationalOrder extends ShipmentOrder {

    private final List<String> customsItems;

    /**
     * Creates an international order template.
     *
     * @param service the shipping service name
     * @param handlingInstructions the instructions printed on the label
     * @param customsItems the goods declared to customs
     */
    InternationalOrder(
            String service,
            List<String> handlingInstructions,
            List<String> customsItems) {
        // Flexible constructor body (JEP 513): fields are set before super()
        // runs, so the subclass is never observed half-initialized.
        this.customsItems = new ArrayList<>(customsItems);
        super(service, handlingInstructions);
    }

    private InternationalOrder(InternationalOrder source) {
        this.customsItems = new ArrayList<>(source.customsItems);
        super(source);
    }

    @Override
    InternationalOrder copy() {
        return new InternationalOrder(this);
    }

    /**
     * Declares one more item to customs.
     *
     * @param item the declared good
     */
    void declareCustomsItem(String item) {
        customsItems.add(item);
    }

    List<String> customsItems() {
        return List.copyOf(customsItems);
    }
}

// ── Prototype registry ──────────────────────────────────────────────────────

/** Closed set of order templates the company offers. */
enum TemplateKind {
    EXPRESS_DOMESTIC("express-domestic"),
    FRAGILE_DOMESTIC("fragile-domestic"),
    INTERNATIONAL_ELECTRONICS("international-electronics");

    private final String label;

    TemplateKind(String label) {
        this.label = label;
    }

    /**
     * Parses user input into a template kind, ignoring case and spaces.
     *
     * @param label the raw template name, such as "express-domestic"
     * @return the matching kind, or empty when no such template exists
     */
    static Optional<TemplateKind> fromLabel(String label) {
        return Arrays.stream(values())
                .filter(kind -> kind.label.equalsIgnoreCase(label.strip()))
                .findFirst();
    }
}

/** Keeps one prototype per template kind and hands out copies of it. */
static final class PrototypeRegistry {

    private final Map<TemplateKind, ShipmentOrder> prototypes;

    /**
     * Stores private copies of the prototypes, so callers cannot mutate them.
     *
     * @param prototypes one prototype for every template kind
     * @throws IllegalArgumentException when a template kind has no prototype
     */
    PrototypeRegistry(Map<TemplateKind, ShipmentOrder> prototypes) {
        var missing = EnumSet.allOf(TemplateKind.class);
        missing.removeAll(prototypes.keySet());
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("Missing prototypes for " + missing);
        }

        this.prototypes = new EnumMap<>(TemplateKind.class);
        prototypes.forEach((kind, prototype) -> this.prototypes.put(kind, prototype.copy()));
    }

    /**
     * Creates a new order from the prototype of the given kind.
     *
     * @param kind the template kind
     * @return an independent copy of the prototype
     */
    ShipmentOrder spawn(TemplateKind kind) {
        return prototypes.get(kind).copy();
    }
}

/**
 * Builds the registry with the company's standard templates.
 *
 * @return the populated registry
 */
static PrototypeRegistry standardRegistry() {
    return new PrototypeRegistry(Map.of(
            TemplateKind.EXPRESS_DOMESTIC,
            new DomesticOrder(EXPRESS_SERVICE, List.of(SIGNATURE_REQUIRED)),

            TemplateKind.FRAGILE_DOMESTIC,
            new DomesticOrder(STANDARD_SERVICE, List.of(HANDLE_WITH_CARE, KEEP_UPRIGHT)),

            TemplateKind.INTERNATIONAL_ELECTRONICS,
            new InternationalOrder(
                    INTERNATIONAL_SERVICE,
                    List.of(HANDLE_WITH_CARE, CONTAINS_LITHIUM_BATTERIES),
                    List.of(LAPTOP))));
}

// ── Client ──────────────────────────────────────────────────────────────────

/**
 * Renders an order as a single line. The switch is exhaustive over the sealed
 * hierarchy: a new order type without a rendering is a compile error.
 *
 * @param order the order to render
 * @return the formatted line
 */
static String render(ShipmentOrder order) {
    var base = "%s to %s at %s %s".formatted(
            order.service(),
            order.recipient(),
            order.destination(),
            order.handlingInstructions());

    return switch (order) {
        case DomesticOrder _ -> "[domestic] " + base;
        case InternationalOrder international ->
                "[international] %s customs=%s".formatted(base, international.customsItems());
    };
}

/**
 * Creates an order from a template name typed by the user.
 *
 * @param registry the registry holding the prototypes
 * @param templateLabel the raw template name, such as "fragile-domestic"
 * @return the new order, or empty when no such template exists
 */
static Optional<ShipmentOrder> orderFrom(PrototypeRegistry registry, String templateLabel) {
    return TemplateKind.fromLabel(templateLabel).map(kind -> registry.spawn(kind));
}

void main() {
    var registry = standardRegistry();

    var vase = registry.spawn(TemplateKind.FRAGILE_DOMESTIC);
    vase.assignRecipient("Ana", "Recife");

    var mirror = registry.spawn(TemplateKind.FRAGILE_DOMESTIC);
    mirror.assignRecipient("Bruno", "Porto Alegre");
    mirror.addHandlingInstruction(SIGNATURE_REQUIRED);

    var gadgets = registry.spawn(TemplateKind.INTERNATIONAL_ELECTRONICS);
    gadgets.assignRecipient("Carla", "Lisbon");
    if (gadgets instanceof InternationalOrder international) {
        international.declareCustomsItem(SMARTPHONE);
    }

    IO.println(render(vase));
    IO.println(render(mirror));
    IO.println(render(gadgets));
    IO.println(render(registry.spawn(TemplateKind.INTERNATIONAL_ELECTRONICS)));
    IO.println(orderFrom(registry, "same-day-drone")
            .map(order -> render(order))
            .orElse("Unknown template: same-day-drone"));
}
