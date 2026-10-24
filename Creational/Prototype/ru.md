# Паттерн проектирования Prototype

[**English Version**](/Creational/Prototype/README.md) </br>
[**Versão em Português**](/Creational/Prototype/pt.md)

Добро пожаловать в раздел, посвящённый паттерну проектирования Prototype (Прототип)! Здесь вы найдёте подробные объяснения, примеры и практические идеи о паттерне Prototype, включая его применение в реальных сценариях. Раздел предназначен как для начинающих, так и для опытных специалистов, которые хотят углубить свои знания о паттернах проектирования.

## Содержание

- [Введение](#введение)
- [Паттерн Prototype](#паттерн-prototype)
  - [Концепция](#концепция)
  - [Поверхностное и глубокое копирование](#поверхностное-и-глубокое-копирование)
  - [Почему не `Cloneable`?](#почему-не-cloneable)
  - [Реализация на Java 25](#реализация-на-java-25)
  - [Запуск](#запуск)
- [Применение](#применение)
- [Преимущества](#преимущества)
- [Участие в проекте](#участие-в-проекте)

## Введение

Этот раздел посвящён изучению паттерна Prototype. Он продолжает логистическую тему других порождающих паттернов и реализован на **Java 25** с sealed-иерархией классов, копирующими конструкторами, flexible constructor bodies и исчерпывающим pattern matching.

## Паттерн Prototype

### Концепция

Prototype — порождающий паттерн, который создаёт новые объекты **копированием существующего, заранее настроенного экземпляра** (прототипа), а не построением с нуля. Клиент просит прототип скопировать себя, поэтому ему не нужно знать конкретный копируемый класс.

Паттерн особенно полезен, когда:
- Создание объекта дорого или требует множества шагов настройки.
- Системе нужно много объектов, которые лишь немного отличаются от нескольких шаблонов.
- Конкретный класс известен только во время выполнения, и клиент должен оставаться от него независимым.

### Поверхностное и глубокое копирование

**Поверхностная** копия разделяет изменяемые поля (например, `List`) с оригиналом: изменение списка копии меняет и прототип. **Глубокая** копия дублирует такие поля, поэтому каждая копия полностью независима. Эта реализация всегда глубоко копирует изменяемое состояние, а реестр хранит собственные копии полученных прототипов.

### Почему не `Cloneable`?

Механизм `Cloneable`/`Object.clone()` в Java сломан: у `Cloneable` нет метода `clone()`, `clone()` создаёт объекты без вызова конструктора, по умолчанию копирует поверхностно и конфликтует с `final`-полями. *Effective Java* (3-е изд., Item 13) рекомендует **копирующие конструкторы** или копирующие фабрики, что и используется в `copy()`.

### Реализация на Java 25

Логистическая компания хранит заранее настроенные заказы на доставку. Новые заказы — это копии шаблона, в которых сотрудник указывает только получателя и адрес:

| Шаблон | Тип | Сервис | Инструкции по обращению | Таможня |
|---|---|---|---|---|
| `EXPRESS_DOMESTIC` | `DomesticOrder` | Express | Signature required | — |
| `FRAGILE_DOMESTIC` | `DomesticOrder` | Standard | Handle with care, Keep upright | — |
| `INTERNATIONAL_ELECTRONICS` | `InternationalOrder` | International Priority | Handle with care, Contains lithium batteries | Laptop |

Ключевые фрагменты (полный код в [`Prototype.java`](/Creational/Prototype/Prototype.java)):

```java
abstract static sealed class ShipmentOrder permits DomesticOrder, InternationalOrder {

    private final String service;
    private final List<String> handlingInstructions;
    private String recipient = UNASSIGNED;
    private String destination = UNASSIGNED;

    // Copy constructor: deep-copies mutable fields.
    protected ShipmentOrder(ShipmentOrder source) {
        this.service = source.service;
        this.handlingInstructions = new ArrayList<>(source.handlingInstructions);
        this.recipient = source.recipient;
        this.destination = source.destination;
    }

    abstract ShipmentOrder copy();
}

static final class InternationalOrder extends ShipmentOrder {

    private final List<String> customsItems;

    private InternationalOrder(InternationalOrder source) {
        this.customsItems = new ArrayList<>(source.customsItems); // before super() (JEP 513)
        super(source);
    }

    @Override
    InternationalOrder copy() { // covariant return type
        return new InternationalOrder(this);
    }
}

static final class PrototypeRegistry {

    private final Map<TemplateKind, ShipmentOrder> prototypes;

    // The client gets a copy without knowing the concrete class.
    ShipmentOrder spawn(TemplateKind kind) {
        return prototypes.get(kind).copy();
    }
}
```

Замечания по дизайну:
- **Вложенные классы `static`**: в compact source file каждое объявление верхнего уровня становится членом неявного класса. Records, enums и интерфейсы неявно static, а классы нет, поэтому они объявлены `static`, чтобы не захватывать неявный внешний экземпляр.
- **Sealed-иерархия**: `render` выполняет исчерпывающий `switch` по `ShipmentOrder`; новый тип заказа без отображения — ошибка компиляции.
- **Flexible constructor bodies** (JEP 513, финализирован в Java 25) позволяют `InternationalOrder` инициализировать свои поля *до* вызова `super(...)`, поэтому объект никогда не наблюдается частично инициализированным.
- **Реестр полный**: конструктор отклоняет карту, в которой нет какого-либо `TemplateKind` (нарушение инварианта), поэтому `spawn` никогда не возвращает `null`. Неизвестный ввод пользователя превращается в `Optional.empty()` в `TemplateKind.fromLabel`.

### Запуск

Файл является compact source file Java 25 (JEP 512) и запускается без инструмента сборки:

```bash
java Creational/Prototype/Prototype.java
```

Вывод (обратите внимание: вторая копия `INTERNATIONAL_ELECTRONICS` не затронута изменениями первой):

```text
[domestic] Standard to Ana at Recife [Handle with care, Keep upright]
[domestic] Standard to Bruno at Porto Alegre [Handle with care, Keep upright, Signature required]
[international] International Priority to Carla at Lisbon [Handle with care, Contains lithium batteries] customs=[Laptop, Smartphone]
[international] International Priority to <unassigned> at <unassigned> [Handle with care, Contains lithium batteries] customs=[Laptop]
Unknown template: same-day-drone
```

## Применение

Паттерн Prototype широко используется, например, в:

- **Логистике и e-commerce:** создание заказов, этикеток и расчётов стоимости из заранее настроенных шаблонов.
- **Редакторах документов:** дублирование фигур, слайдов или страниц со всем форматированием.
- **Разработке игр:** создание врагов, предметов или частиц из заранее настроенных архетипов.
- **Конфигурации:** получение конфигураций для разных окружений из базовой конфигурации.
- **Тестировании:** построение фикстур копированием корректного базового объекта с изменением только нужного.

## Преимущества

- **Независимость от конкретных классов:** клиенты копируют объекты через `copy()`, не зная их тип.
- **Дешёвое создание:** дорогая настройка выполняется один раз в прототипе и переиспользуется каждой копией.
- **Меньше подклассов и конструкторов:** вариации выражаются зарегистрированными прототипами, а не новыми классами.
- **Гибкость во время выполнения:** прототипы можно добавлять, заменять и настраивать во время работы приложения.
- **Компромисс:** каждый класс иерархии должен корректно реализовать копирование, а глубокое копирование графов объектов с циклами требует особой осторожности.

## Участие в проекте

Мы приветствуем вклад в проект! Если вы хотите добавить новые объяснения, улучшения или исправления, выполните следующие шаги:

1. Сделайте форк этого репозитория.
2. Создайте ветку для изменений: `git checkout -b feature/new-example`.
3. Откройте pull request с понятным описанием изменений и их мотивации.

Делитесь своим опытом и помогите нам сделать этот раздел лучше. Вместе мы можем создать ценный ресурс для всех, кто интересуется паттернами проектирования! 🚀
