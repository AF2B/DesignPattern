# Padrão de Design Prototype

[**English Version**](/Creational/Prototype/README.md) </br>
[**Pусская версия**](/Creational/Prototype/ru.md)

Bem-vindo à seção do Padrão de Design Prototype! Aqui você encontrará explicações detalhadas, exemplos e insights sobre o padrão Prototype, incluindo suas aplicações em cenários do mundo real. Esta seção é destinada tanto para iniciantes quanto para profissionais experientes que desejam aprofundar seu conhecimento sobre padrões de design.

## Índice

- [Introdução](#introdução)
- [Padrão de Design Prototype](#padrão-de-design-prototype-1)
  - [Conceito](#conceito)
  - [Cópia Rasa vs. Cópia Profunda](#cópia-rasa-vs-cópia-profunda)
  - [Por Que Não `Cloneable`?](#por-que-não-cloneable)
  - [Implementação em Java 25](#implementação-em-java-25)
  - [Executando](#executando)
- [Aplicações](#aplicações)
- [Vantagens](#vantagens)
- [Contribuições](#contribuições)

## Introdução

Esta seção é dedicada à exploração e compreensão do padrão Prototype. Ela segue o tema de logística dos outros padrões criacionais, implementado em **Java 25** com hierarquia de classes seladas, construtores de cópia, flexible constructor bodies e pattern matching exaustivo.

## Padrão de Design Prototype

### Conceito

O Prototype é um padrão criacional que cria novos objetos **copiando uma instância existente e pré-configurada** (o protótipo) em vez de construí-los do zero. O cliente pede ao protótipo que se copie, então nunca precisa conhecer a classe concreta copiada.

Este padrão é particularmente útil quando:
- Criar um objeto é caro ou exige muitos passos de configuração.
- O sistema precisa de muitos objetos que diferem pouco de alguns poucos modelos.
- A classe concreta só é conhecida em tempo de execução, e o cliente deve permanecer desacoplado dela.

### Cópia Rasa vs. Cópia Profunda

Uma cópia **rasa** compartilha campos mutáveis (como uma `List`) com o original: alterar a lista da cópia também altera o protótipo. Uma cópia **profunda** duplica esses campos, então cada cópia é totalmente independente. Esta implementação sempre faz cópia profunda do estado mutável, e o registro guarda cópias privadas dos protótipos que recebe.

### Por Que Não `Cloneable`?

O `Cloneable`/`Object.clone()` do Java é um mecanismo quebrado: `Cloneable` não tem o método `clone()`, `clone()` cria objetos sem chamar construtor, é raso por padrão e conflita com campos `final`. *Effective Java* (3ª ed., Item 13) recomenda **construtores de cópia** ou fábricas de cópia, que é o que `copy()` usa aqui.

### Implementação em Java 25

Uma empresa de logística mantém pedidos de envio pré-configurados. Novos pedidos são cópias de um modelo que o atendente só personaliza com destinatário e destino:

| Modelo | Tipo | Serviço | Instruções de manuseio | Alfândega |
|---|---|---|---|---|
| `EXPRESS_DOMESTIC` | `DomesticOrder` | Express | Signature required | — |
| `FRAGILE_DOMESTIC` | `DomesticOrder` | Standard | Handle with care, Keep upright | — |
| `INTERNATIONAL_ELECTRONICS` | `InternationalOrder` | International Priority | Handle with care, Contains lithium batteries | Laptop |

Trechos principais (código completo em [`Prototype.java`](/Creational/Prototype/Prototype.java)):

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

Notas de design:
- **Classes aninhadas `static`**: num compact source file, toda declaração de topo vira membro de uma classe implícita. Records, enums e interfaces são implicitamente static, mas classes não, então são declaradas `static` para não capturar uma instância externa implícita.
- **Hierarquia selada**: `render` faz `switch` exaustivo sobre `ShipmentOrder`; um novo tipo de pedido sem renderização é erro de compilação.
- **Flexible constructor bodies** (JEP 513, final no Java 25) permitem que `InternationalOrder` inicialize seus próprios campos *antes* de `super(...)`, então o objeto nunca é observado meio inicializado.
- **O registro é total**: o construtor rejeita um mapa sem algum `TemplateKind` (violação de invariante), então `spawn` nunca retorna `null`. Entrada desconhecida do usuário vira `Optional.empty()` em `TemplateKind.fromLabel`.

### Executando

O arquivo é um compact source file do Java 25 (JEP 512), executável sem ferramenta de build:

```bash
java Creational/Prototype/Prototype.java
```

Saída (note que a segunda cópia de `INTERNATIONAL_ELECTRONICS` não é afetada pelas mudanças feitas na primeira):

```text
[domestic] Standard to Ana at Recife [Handle with care, Keep upright]
[domestic] Standard to Bruno at Porto Alegre [Handle with care, Keep upright, Signature required]
[international] International Priority to Carla at Lisbon [Handle with care, Contains lithium batteries] customs=[Laptop, Smartphone]
[international] International Priority to <unassigned> at <unassigned> [Handle with care, Contains lithium batteries] customs=[Laptop]
Unknown template: same-day-drone
```

## Aplicações

O padrão Prototype é amplamente utilizado em várias aplicações, como:

- **Logística e E-commerce:** Criar pedidos, etiquetas e cotações a partir de modelos pré-configurados.
- **Editores de Documentos:** Duplicar formas, slides ou páginas com toda a formatação.
- **Desenvolvimento de Jogos:** Gerar inimigos, itens ou partículas a partir de arquétipos pré-configurados.
- **Configuração:** Derivar configurações por ambiente a partir de uma configuração base.
- **Testes:** Montar fixtures copiando um objeto base válido e alterando só o que o teste precisa.

## Vantagens

- **Desacoplamento das Classes Concretas:** Clientes copiam objetos via `copy()` sem conhecer seu tipo.
- **Criação Mais Barata:** A configuração cara é feita uma vez no protótipo e reaproveitada por toda cópia.
- **Menos Subclasses e Construtores:** Variações viram protótipos registrados em vez de novas classes.
- **Flexibilidade em Tempo de Execução:** Protótipos podem ser adicionados, substituídos ou configurados com a aplicação rodando.
- **Trade-off:** Toda classe da hierarquia precisa implementar a cópia corretamente, e cópias profundas de grafos de objetos com ciclos exigem cuidado extra.

## Contribuições

Contribuições são bem-vindas! Se você deseja adicionar novas explicações, melhorias ou correções, siga estes passos:

1. Faça um fork deste repositório.
2. Crie uma branch para suas alterações: `git checkout -b feature/new-example`.
3. Abra um pull request descrevendo claramente as alterações feitas e a motivação por trás delas.

Sinta-se à vontade para compartilhar seu conhecimento e nos ajudar a enriquecer esta seção. Juntos, podemos criar um recurso valioso para todos os interessados em padrões de design! 🚀
