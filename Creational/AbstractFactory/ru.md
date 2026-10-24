# Паттерн проектирования Abstract Factory

[**English Version**](/Creational/AbstractFactory/README.md) </br>
[**Versão em Português**](/Creational/AbstractFactory/pt.md)

Добро пожаловать в раздел, посвящённый паттерну проектирования Abstract Factory (Абстрактная фабрика)! Здесь вы найдёте подробные объяснения, примеры и практические идеи о паттерне Abstract Factory, включая его применение в реальных сценариях. Раздел предназначен как для начинающих, так и для опытных специалистов, которые хотят углубить свои знания о паттернах проектирования.

## Содержание

- [Введение](#введение)
- [Паттерн Abstract Factory](#паттерн-abstract-factory)
  - [Концепция](#концепция)
  - [Factory и Abstract Factory](#factory-и-abstract-factory)
  - [Реализация на Java 25](#реализация-на-java-25)
  - [Запуск](#запуск)
- [Применение](#применение)
- [Преимущества](#преимущества)
- [Участие в проекте](#участие-в-проекте)

## Введение

Этот раздел посвящён изучению паттерна Abstract Factory. Он продолжает логистическую тему раздела [Factory](/Creational/Factory/README.md), теперь на **Java 25** с использованием sealed-интерфейсов, records, исчерпывающего `switch` и record patterns.

## Паттерн Abstract Factory

### Концепция

Abstract Factory — порождающий паттерн, который предоставляет интерфейс для создания **семейств связанных объектов**, не указывая их конкретные классы. Каждая конкретная фабрика создаёт полное и согласованное семейство, поэтому клиентский код никогда не сможет объединить продукты, которые не подходят друг к другу.

Паттерн особенно полезен, когда:
- Система должна работать с несколькими семействами связанных продуктов.
- Продукты одного семейства должны использоваться вместе, а смешивание семейств является ошибкой.
- Конкретное семейство известно только во время выполнения (конфигурация, ввод пользователя, окружение).

### Factory и Abstract Factory

- **Factory** создаёт **один** продукт, выбранный по параметру (`delivery-factory "air"` возвращает `AirDelivery`).
- **Abstract Factory** создаёт **семейство** продуктов через один объект (`AirLogistics` создаёт `CargoPlane`, `PressurizedCrate` и `FlightRadar`). Выбор фабрики один раз фиксирует всё семейство.

### Реализация на Java 25

Логистическая компания отправляет посылки по воздуху, суше и морю. Каждому виду транспорта нужны перевозчик, упаковка и канал отслеживания, которые имеют смысл только вместе:

| Семейство | Перевозчик | Упаковка | Трекер |
|---|---|---|---|
| `AirLogistics` | `CargoPlane` | `PressurizedCrate` (500 кг) | `FlightRadar` |
| `LandLogistics` | `Truck` | `Pallet` (1 000 кг) | `GpsTracker` |
| `SeaLogistics` | `CargoShip` | `ShippingContainer` (28 000 кг) | `AisTransponder` |

Ключевые фрагменты (полный код в [`AbstractFactory.java`](/Creational/AbstractFactory/AbstractFactory.java)):

```java
sealed interface LogisticsFactory permits AirLogistics, LandLogistics, SeaLogistics {
    Carrier createCarrier();
    Packaging createPackaging();
    Tracker createTracker();
}

record AirLogistics() implements LogisticsFactory {
    public Carrier createCarrier() { return new CargoPlane(); }
    public Packaging createPackaging() { return new PressurizedCrate(); }
    public Tracker createTracker() { return new FlightRadar(); }
}

// Exhaustive: adding a TransportMode without a factory is a compile error.
static LogisticsFactory factoryFor(TransportMode mode) {
    return switch (mode) {
        case AIR -> new AirLogistics();
        case LAND -> new LandLogistics();
        case SEA -> new SeaLogistics();
    };
}

// The client only sees abstract products.
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
```

Замечания по дизайну:
- **Sealed-интерфейсы** закрывают каждую иерархию продуктов и иерархию фабрик, поэтому компилятор проверяет исчерпываемость каждого `switch` по ним.
- **Ожидаемые ошибки — это значения**: посылка с превышением веса возвращает `Overweight`, а неизвестный вид транспорта возвращает `Optional.empty()` из `TransportMode.fromLabel`. Никаких исключений для бизнес-правил.
- **Деньги хранятся в `BigDecimal`**, никогда в `double`.

### Запуск

Файл является compact source file Java 25 (JEP 512) и запускается без инструмента сборки:

```bash
java Creational/AbstractFactory/AbstractFactory.java
```

Вывод:

```text
Scheduled: Cargo Plane + Pressurized Crate, cost 500.00, tracked by Flight radar to Pernambuco
Scheduled: Truck + Pallet, cost 100.00, tracked by GPS on the road to Rio Grande Do Sul
Rejected: 2000 kg exceeds the Pressurized Crate limit of 500 kg
Scheduled: Cargo Ship + Shipping Container, cost 200.00, tracked by AIS transponder to Port of Santos
Unknown transport mode: space
```

## Применение

Паттерн Abstract Factory широко используется, например, в:

- **Логистических системах:** создание согласованных наборов перевозчик/упаковка/отслеживание для каждого вида транспорта.
- **UI-тулкитах:** создание кнопок, меню и диалогов в едином стиле (светлая и тёмная тема или набор виджетов для каждой ОС).
- **Слоях хранения данных:** создание соединений, команд и транзакций для одного поставщика СУБД.
- **Облачных провайдерах:** создание клиентов хранилища, очередей и вычислений одного провайдера за общими интерфейсами.
- **Тестировании:** замена целого семейства реальной инфраструктуры на in-memory заглушки одной фабрикой.

## Преимущества

- **Согласованность продуктов:** семейство создаётся одной фабрикой, поэтому несовместимые комбинации невозможны.
- **Изоляция конкретных классов:** клиенты зависят только от абстрактных продуктов и абстрактной фабрики.
- **Простая замена семейства:** смена экземпляра фабрики меняет всё семейство сразу.
- **Открытость/закрытость для семейств:** новое семейство — это новая фабрика и её продукты, без изменения клиентского кода.
- **Компромисс:** добавление нового *вида* продукта (например, `Insurance`) меняет интерфейс фабрики и все конкретные фабрики. Sealed-иерархия заставляет компилятор указать каждое место, которое нужно изменить.

## Участие в проекте

Мы приветствуем вклад в проект! Если вы хотите добавить новые объяснения, улучшения или исправления, выполните следующие шаги:

1. Сделайте форк этого репозитория.
2. Создайте ветку для изменений: `git checkout -b feature/new-example`.
3. Откройте pull request с понятным описанием изменений и их мотивации.

Делитесь своим опытом и помогите нам сделать этот раздел лучше. Вместе мы можем создать ценный ресурс для всех, кто интересуется паттернами проектирования! 🚀
