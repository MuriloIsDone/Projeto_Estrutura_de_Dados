package service;

import estruturas.FilaCircular;
import estruturas.Pilha;
import model.Operacao;
import model.Solicitacao;

// regra de negocio, usa so as duas estruturas do prof (fila e pilha em vetor)
// n pode usar Collections (Stack/Queue/ArrayList etc), o vetor de dentro
// nunca é mexido direto, só via enqueue/dequeue e push/pop mesmo
public class CentralAtendimento {

    private static final int TAM_FILA = 50;
    private static final int TAM_PILHA = 100;

    private FilaCircular<Solicitacao> filaEspera;
    private Pilha<Operacao> historico;

    private int proximoCodigo;
    private int totalCadastradas;
    private int totalAtendidas;

    public CentralAtendimento() {
        this.filaEspera = new FilaCircular<Solicitacao>(TAM_FILA);
        this.historico = new Pilha<Operacao>(TAM_PILHA);
        this.proximoCodigo = 101;   // codigos comecam em 101, como no enunciado
        this.totalCadastradas = 0;
        this.totalAtendidas = 0;
    }

    //cadastrar nova solicitacao
    public Solicitacao cadastrar(String solicitante, String descricao,
                                 String categoria, int prioridade) throws Exception {
        if (filaEspera.qIsFull())
            throw new Exception("A fila de espera esta cheia (capacidade "
                    + TAM_FILA + "). Atenda solicitacoes antes de cadastrar novas.");
        verificaEspacoHistorico();

        Solicitacao s = new Solicitacao(proximoCodigo, solicitante, descricao,
                                        categoria, prioridade);
        filaEspera.enqueue(s);                              // entra no FIM da fila
        registrarOperacao(new Operacao(Operacao.CADASTRO, s));
        proximoCodigo++;
        totalCadastradas++;
        return s;
    }

    //consultar proxima solicitacao (sem remover neh)
    public Solicitacao consultarProxima() throws Exception {
        if (filaEspera.qIsEmpty())
            throw new Exception("Nao ha solicitacoes aguardando atendimento.");
        return filaEspera.front();
    }

    //atender proxima solicitacao (remove do inicio da fila - fifo)
    public Solicitacao atenderProxima(String responsavel) throws Exception {
        if (filaEspera.qIsEmpty())
            throw new Exception("Nao ha solicitacoes na fila para atender.");
        verificaEspacoHistorico();

        Solicitacao s = filaEspera.dequeue(); // sempre tira quem chegou primeiro
        s.setResponsavel(responsavel);
        s.setStatus(Solicitacao.CONCLUIDA);
        registrarOperacao(new Operacao(Operacao.ATENDIMENTO, s));
        totalAtendidas++;
        return s;
    }

    // percorre a fila girando: tira do inicio e bota no fim, dá a volta
    // toda e a fila volta igualzinha, é o unico jeito de "olhar tudo" sem
    // um iterator na estrutura
    public String filaFormatada() throws Exception {
        if (filaEspera.qIsEmpty())
            return "  (fila vazia)";

        StringBuilder sb = new StringBuilder();
        int qtde = filaEspera.totalElementos();
        for (int i = 0; i < qtde; i++) {
            Solicitacao s = filaEspera.dequeue();
            sb.append("  ").append(i + 1).append("o da fila -> ").append(s).append("\n");
            filaEspera.enqueue(s);
        }
        return sb.toString();
    }

    // quantidade de solicitacoes aguardando
    public int quantidadeAguardando() {
        return filaEspera.totalElementos();
    }

    public boolean filaVazia() {
        return filaEspera.qIsEmpty();
    }

    //consultar ultima operacao realizada (topo da pilha, sem remover)
    public Operacao ultimaOperacao() throws Exception {
        if (historico.isEmpty())
            throw new Exception("Nenhuma operacao foi realizada ainda.");
        return historico.topo();
    }

    // desempilha tudo pra uma pilha auxiliar (e imprime no caminho), dps
    // devolve pra pilha original, assim inverte a ordem pra mostrar e desinverte de novo pra n perder nada
    public String historicoFormatado() throws Exception {
        if (historico.isEmpty())
            return "  (nenhuma operacao registrada)";

        Pilha<Operacao> aux = new Pilha<Operacao>(TAM_PILHA);
        StringBuilder sb = new StringBuilder();

        while (!historico.isEmpty()) {
            Operacao op = historico.pop();
            sb.append("  ").append(op).append("\n");
            aux.push(op);
        }
        while (!aux.isEmpty())          // devolve na ordem correta
            historico.push(aux.pop());

        return sb.toString();
    }

    // só desfaz ATENDIMENTO. a solicitacao volta pro inicio da fila (onde
    // ela ja tava antes de ser atendida), por isso a fila auxiliar
    public Solicitacao desfazerUltimaOperacao() throws Exception {
        if (historico.isEmpty())
            throw new Exception("Nao ha operacoes para desfazer.");

        Operacao op = historico.topo(); // só espia antes de decidir se pode desfazer
        if (!op.getTipo().equals(Operacao.ATENDIMENTO))
            throw new Exception("A ultima operacao foi um " + op.getTipo()
                    + ". Somente operacoes de ATENDIMENTO podem ser desfeitas.");

        if (filaEspera.qIsFull())
            throw new Exception("A fila esta cheia: nao ha espaco para devolver a solicitacao.");

        historico.pop();                        // agora sim a operacao sai da pilha

        Solicitacao s = op.getSolicitacao();
        s.setStatus(Solicitacao.AGUARDANDO);
        s.setResponsavel("-");

        // como só tem enqueue (que bota no fim), monto uma fila aux com a
        // solicitacao na frente e o resto da fila atras, dps devolvo tudo
        FilaCircular<Solicitacao> aux = new FilaCircular<Solicitacao>(TAM_FILA);
        aux.enqueue(s);
        while (!filaEspera.qIsEmpty())
            aux.enqueue(filaEspera.dequeue());
        while (!aux.qIsEmpty())
            filaEspera.enqueue(aux.dequeue());

        totalAtendidas--;
        return s;
    }

    // resumo do sistema
    public String resumo() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Solicitacoes cadastradas ..: ").append(totalCadastradas).append("\n");
        sb.append("  Solicitacoes atendidas ....: ").append(totalAtendidas).append("\n");
        sb.append("  Aguardando na fila ........: ").append(filaEspera.totalElementos())
          .append(" de ").append(TAM_FILA).append("\n");
        sb.append("  Operacoes no historico ....: ").append(historico.sizeElements())
          .append(" de ").append(TAM_PILHA);
        return sb.toString();
    }

    // confere antes de mexer na fila, senao corre risco de tirar/inserir
    // e so dps descobrir q n da pra registrar a operacao
    private void verificaEspacoHistorico() throws Exception {
        if (historico.isFull())
            throw new Exception("O historico de operacoes esta cheio (capacidade "
                    + TAM_PILHA + "). Desfaca ou reinicie o sistema.");
    }

    private void registrarOperacao(Operacao op) throws Exception {
        historico.push(op);
    }
}
