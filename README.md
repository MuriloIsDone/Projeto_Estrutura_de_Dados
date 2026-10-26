# Central de Atendimento e Serviços — 2º bimestre

Projeto Integrador de **Estruturas de Dados I** — Ciência da Computação, Universidade Presbiteriana Mackenzie (2026/2).

Aplicação Java de console que gerencia solicitações de atendimento. No 2º bimestre as estruturas em vetor foram
**substituídas** pelas versões dinâmicas e encadeadas fornecidas pelo professor, e o sistema ganhou controle de
prioridades, cadastro geral e histórico navegável.

## Estruturas utilizadas (todas fornecidas pelo professor, sem alteração, apenas o pacote)

| Estrutura | Uso no sistema |
|---|---|
| `Fila<T>` (2 instâncias) | solicitações aguardando atendimento, uma fila por prioridade (1 = alta, 2 = baixa) |
| `Pilha<T>` | histórico recente de operações e desfazimento |
| `LinkedList<T>` (lista simples) | cadastro geral de solicitações (inserção, busca, percurso e remoção) |
| `DLL<T>` (lista dupla) | histórico navegável dos atendimentos concluídos (percurso nos dois sentidos) |

Nenhuma estrutura da Java Collections Framework é usada. Os arquivos `Pilha.java`, `Fila.java`, `LinkedList.java`,
`DLL.java`, `Node.java` e `NodeDLL.java` são as classes do professor; a `LinkedList` do projeto é a dele, **não** a `java.util.LinkedList`.

## Organização

```
src
├── app         Main.java
├── ui          Menu.java                 (entrada e saída; sem regra de negócio)
├── service     CentralAtendimento.java   (regras e integração das 4 estruturas)
│               ControleFilas.java        (duas filas, seleção e anti-inanição)
├── model       Solicitacao, Atendimento, Operacao
├── estruturas  Node, NodeDLL, Pilha, Fila, LinkedList, DLL   (professor)
├── excecoes    exceções específicas do sistema
└── testes      TestesB2.java             (76 testes automatizados)
                ExperimentoPollLast.java  (reproduz um defeito do pollLast() da LinkedList do professor)
evidencias      saídas reais de execução usadas no relatório
verificar_mutacoes.sh   introduz defeitos de propósito numa cópia e confirma que os testes os detectam
```

## Regra de prioridades

1. Dentro de cada fila vale rigorosamente a ordem FIFO.
2. Atende-se a fila de prioridade 1; se estiver vazia, a de prioridade 2.
3. **Anti-inanição:** após 3 atendimentos seguidos da fila 1 *com a fila 2 esperando*, atende-se 1 da fila 2.
   O valor 3 está na constante `ControleFilas.LIMITE_ATENDIMENTOS_ALTA_SEGUIDOS`.
4. Desfazer devolve a solicitação ao **início** da fila da sua prioridade e restaura o contador.

## Como executar

```bash
javac -encoding UTF-8 -d bin $(find src -name "*.java")
java -cp bin app.Main                  # sistema
java -cp bin testes.TestesB2           # testes automatizados
java -cp bin testes.ExperimentoPollLast  # defeito do pollLast() (por isso o sistema não o utiliza)
sh verificar_mutacoes.sh               # prova que os testes detectam defeitos
```

No Eclipse: `File > Import > Existing Projects into Workspace`, escolha a pasta do projeto e execute `app.Main`.
O projeto usa UTF-8 (já configurado em `.settings`).

## Assunções (marcadas porque o enunciado deixa em aberto)

- **Duas filas (prioridades 1 e 2)**, conforme o item 19. O adendo cita "P1 a P5"; considerou-se erro de digitação.
- Lista simples **linear** (a `LinkedList` do professor não é circular).
- Solicitações `CONCLUIDA` não podem ser removidas do cadastro (fazem parte do histórico de atendimentos);
  os registros da lista dupla só saem pelo desfazimento.
