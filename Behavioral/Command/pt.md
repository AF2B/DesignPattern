# Padrão de Design Command

[**English Version**](/Behavioral/Command/README.md) </br>
[**Pусская версия**](/Behavioral/Command/ru.md)

Bem-vindo à seção sobre o Padrão de Design Command! Aqui você encontrará explicações detalhadas, opiniões e insights sobre o padrão de design Command, incluindo suas aplicações em cenários do mundo real. Esta seção é destinada tanto para iniciantes quanto para profissionais experientes que desejam aprimorar seu conhecimento sobre padrões de design.

## Índice

- [Introdução](#introdução)
- [Padrão de Design Command](#padrão-de-design-command)
  - [Conceito](#conceito)
- [Aplicações](#aplicações)
- [Vantagens](#vantagens)
- [Contribuições](#contribuições)

## Introdução

Esta seção é dedicada ao estudo e compreensão do padrão de design Command. Cada aspecto é explicado em detalhes, com exemplos práticos e casos de uso. Aqui, você encontrará não apenas implementações, mas também análises de desempenho e melhores práticas para utilizar o padrão Command.

## Padrão de Design Command

### Conceito

O padrão de design Command encapsula uma solicitação como um objeto independente, de modo que quem dispara a solicitação (o invocador) não precisa saber nada sobre quem a executa de fato (o receptor). Como um comando é apenas um objeto, ele pode ser armazenado, enfileirado, repassado, registrado em log e — já que carrega informação suficiente para se reverter — desfeito. O `command.clj` demonstra isso com um pequeno exemplo de edição de texto: `InsertTextCommand` e `DeleteTextCommand` implementam o par `execute`/`undo` do protocolo `Command`, um histórico de comandos (criado com `create-invoker`) mantém os comandos executados em uma pilha para que `undo-last!` possa desfazê-los um a um, e `MacroCommand` compõe vários comandos em um só, executando-os e desfazendo-os como uma única unidade.

## Aplicações

O padrão Command é amplamente utilizado em várias aplicações, tais como:

- Pilhas de desfazer/refazer em editores e outras aplicações com estado
- Filas de tarefas e agendadores de jobs, onde um comando é enfileirado agora e executado depois
- Assistentes (wizards) e gravadores de macro que agrupam várias ações em uma unidade replicável
- Desacoplamento de controles de UI (itens de menu, botões) da lógica que eles disparam

## Vantagens

- **Desacoplamento**: O invocador depende apenas do protocolo `Command`, nunca da lógica concreta do receptor.
- **Composição**: Comandos podem ser combinados (veja `MacroCommand`) ou enfileirados sem alterar sua implementação.
- **Reversibilidade**: Colocar `execute` e `undo` no mesmo objeto mantém a informação necessária para reverter uma ação bem ao lado da própria ação.

## Contribuições

Convidamos contribuições! Se você deseja adicionar novas explicações, melhorias ou correções, siga estes passos:

1. Faça um fork deste repositório.
2. Crie um branch para suas alterações: `git checkout -b feature/new-example`.
3. Abra um pull request descrevendo claramente as alterações feitas e a motivação por trás delas.

Sinta-se à vontade para contribuir com seu conhecimento e ajudar a enriquecer esta seção. Juntos, podemos criar um recurso valioso para todos os interessados em padrões de design!

🚀

---
