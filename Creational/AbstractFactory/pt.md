# Padrão de Design Abstract Factory

[**English Version**](/Creational/AbstractFactory/README.md) </br>
[**Pусская версия**](/Creational/AbstractFactory/ru.md)

Bem-vindo à seção do Padrão de Design Abstract Factory! Aqui você encontrará explicações detalhadas, exemplos e insights sobre o padrão Abstract Factory, incluindo suas aplicações em cenários do mundo real. Esta seção é destinada tanto para iniciantes quanto para profissionais experientes que desejam aprofundar seu conhecimento sobre padrões de design.

## Índice

- [Introdução](#introdução)
- [Padrão de Design Abstract Factory](#padrão-de-design-abstract-factory-1)
  - [Conceito](#conceito)
  - [Factory vs. Abstract Factory](#factory-vs-abstract-factory)
  - [Implementação em Java 25](#implementação-em-java-25)
  - [Executando](#executando)
- [Aplicações](#aplicações)
- [Vantagens](#vantagens)
- [Contribuições](#contribuições)

## Introdução

Esta seção é dedicada à exploração e compreensão do padrão Abstract Factory. Ela continua o tema de logística da seção [Factory](/Creational/Factory/README.md), agora implementado em **Java 25** com sealed interfaces, records, `switch` exaustivo e record patterns.

## Padrão de Design Abstract Factory

### Conceito

O Abstract Factory é um padrão criacional que fornece uma interface para criar **famílias de objetos relacionados** sem especificar suas classes concretas. Cada fábrica concreta produz uma família completa e consistente, de modo que o código cliente nunca consegue combinar produtos que não pertencem ao mesmo conjunto.

Este padrão é particularmente útil quando:
- O sistema precisa trabalhar com várias famílias de produtos relacionados.
- Os produtos de uma família devem ser usados juntos, e misturar famílias é um bug.
- A família concreta só é conhecida em tempo de execução (configuração, entrada do usuário, ambiente).

### Factory vs. Abstract Factory

- **Factory** cria **um** produto, escolhido por um parâmetro (`delivery-factory "air"` retorna um `AirDelivery`).
- **Abstract Factory** cria uma **família** de produtos por meio de um único objeto (`AirLogistics` cria um `CargoPlane`, um `PressurizedCrate` e um `FlightRadar`). Escolher a fábrica uma vez fixa a família inteira.

### Implementação em Java 25

Uma empresa de logística envia encomendas por ar, terra e mar. Cada modal precisa de um transportador, uma embalagem e um canal de rastreamento que só fazem sentido juntos:

| Família | Transportador | Embalagem | Rastreador |
|---|---|---|---|
| `AirLogistics` | `CargoPlane` | `PressurizedCrate` (500 kg) | `FlightRadar` |
| `LandLogistics` | `Truck` | `Pallet` (1.000 kg) | `GpsTracker` |
| `SeaLogistics` | `CargoShip` | `ShippingContainer` (28.000 kg) | `AisTransponder` |

Trechos principais (código completo em [`AbstractFactory.java`](/Creational/AbstractFactory/AbstractFactory.java)):

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

Notas de design:
- **Sealed interfaces** fecham cada hierarquia de produtos e a hierarquia de fábricas, então o compilador verifica a exaustividade de todo `switch` sobre elas.
- **Falhas esperadas são valores**: uma encomenda acima do peso retorna `Overweight`, e um modal desconhecido retorna `Optional.empty()` em `TransportMode.fromLabel`. Nada de exceções para regras de negócio.
- **Dinheiro usa `BigDecimal`**, nunca `double`.

### Executando

O arquivo é um compact source file do Java 25 (JEP 512), executável sem ferramenta de build:

```bash
java Creational/AbstractFactory/AbstractFactory.java
```

Saída:

```text
Scheduled: Cargo Plane + Pressurized Crate, cost 500.00, tracked by Flight radar to Pernambuco
Scheduled: Truck + Pallet, cost 100.00, tracked by GPS on the road to Rio Grande Do Sul
Rejected: 2000 kg exceeds the Pressurized Crate limit of 500 kg
Scheduled: Cargo Ship + Shipping Container, cost 200.00, tracked by AIS transponder to Port of Santos
Unknown transport mode: space
```

## Aplicações

O padrão Abstract Factory é amplamente utilizado em várias aplicações, como:

- **Sistemas de Logística:** Montar conjuntos consistentes de transportador/embalagem/rastreamento por modal.
- **Toolkits de UI:** Criar botões, menus e diálogos com a mesma aparência (tema claro vs. escuro, ou um conjunto de widgets por sistema operacional).
- **Camadas de Persistência:** Produzir conexões, comandos e transações de um único fornecedor de banco de dados.
- **Provedores de Nuvem:** Criar clientes de storage, filas e computação de um único provedor atrás de interfaces comuns.
- **Testes:** Trocar uma família inteira de infraestrutura real por fakes em memória com uma única fábrica.

## Vantagens

- **Consistência Entre Produtos:** Uma família é criada por uma única fábrica, então combinações incompatíveis não acontecem.
- **Isolamento das Classes Concretas:** Os clientes dependem apenas dos produtos abstratos e da fábrica abstrata.
- **Troca Fácil de Família:** Trocar a instância da fábrica troca a família inteira de uma vez.
- **Aberto/Fechado para Famílias:** Adicionar uma nova família significa adicionar uma nova fábrica e seus produtos, sem tocar no código cliente.
- **Trade-off:** Adicionar um novo *tipo* de produto (ex.: `Insurance`) muda a interface da fábrica e todas as fábricas concretas. A hierarquia selada faz o compilador apontar cada lugar que precisa mudar.

## Contribuições

Contribuições são bem-vindas! Se você deseja adicionar novas explicações, melhorias ou correções, siga estes passos:

1. Faça um fork deste repositório.
2. Crie uma branch para suas alterações: `git checkout -b feature/new-example`.
3. Abra um pull request descrevendo claramente as alterações feitas e a motivação por trás delas.

Sinta-se à vontade para compartilhar seu conhecimento e nos ajudar a enriquecer esta seção. Juntos, podemos criar um recurso valioso para todos os interessados em padrões de design! 🚀
