# Central de Atendimento e Serviços — Estruturas de Dados I

Projeto Integrador (Mackenzie, Ciência da Computação, 2026/2). Aplicação Java de console para gerenciar
solicitações de atendimento. O 1º bimestre usava Pilha e Fila em vetor; no **2º bimestre** elas foram substituídas
pelas versões dinâmicas e encadeadas do professor, e o sistema ganhou prioridades, cadastro geral e histórico navegável.

## Estruturas (classes do professor, sem alteração além da linha `package`)

| Estrutura | Uso |
|---|---|
| `Fila` (2 instâncias) | solicitações aguardando atendimento: uma fila por prioridade (1 = alta, 2 = baixa) |
| `Pilha` | histórico recente de operações e desfazimento |
| `LinkedList` (lista simples) | cadastro geral de solicitações |
| `DLL` (lista dupla) | histórico navegável dos atendimentos concluídos |

Nenhuma coleção da Java Collections Framework é usada. A `LinkedList` deste projeto é a classe do professor, não a `java.util.LinkedList`.

## Pacotes (declarados em cada arquivo)

- `app`: `Main`
- `ui`: `Menu`
- `service`: `CentralAtendimento`, `ControleFilas` (duas filas, seleção da próxima e anti-inanição)
- `model`: `Solicitacao`, `Atendimento`, `Operacao`
- `estruturas`: `Node`, `NodeDLL`, `Pilha`, `Fila`, `LinkedList`, `DLL`
- `excecoes`: `CentralException` (as exceções específicas do sistema ficam declaradas dentro dela, em um único arquivo)
- `testes`: `TestesB2` (76 testes automatizados)

## Regra de prioridades

Dentro de cada fila vale a ordem FIFO. A fila 1 é atendida primeiro; após 3 atendimentos seguidos dela com a fila 2
esperando, atende-se 1 da fila 2 (constante `LIMITE_ATENDIMENTOS_ALTA_SEGUIDOS`, em `ControleFilas`).
O desfazimento devolve a solicitação ao início da fila da sua prioridade e restaura o contador.

## Como executar

```bash
javac -encoding UTF-8 -d bin *.java
java -cp bin app.Main            # sistema
java -cp bin testes.TestesB2     # testes automatizados
```

Se os acentos aparecerem errados no Eclipse: clique com o botão direito no projeto, *Properties > Resource*, e escolha UTF-8.
