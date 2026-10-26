package service;

import estruturas.DLL;
import estruturas.LinkedList;
import estruturas.Node;
import estruturas.NodeDLL;
import estruturas.Pilha;
import excecoes.CampoInvalidoException;
import excecoes.EstruturaInconsistenteException;
import excecoes.OperacaoInvalidaException;
import excecoes.PrioridadeInvalidaException;
import excecoes.SemRegistroException;
import excecoes.SemSolicitacaoException;
import excecoes.SolicitacaoNaoEncontradaException;
import model.Atendimento;
import model.Operacao;
import model.Solicitacao;

/**
 * Regras de negócio da Central de Atendimento (2º bimestre).
 *
 * Integração das quatro estruturas encadeadas fornecidas pelo professor:
 *  - Fila (duas instâncias, via ControleFilas): solicitações aguardando
 *    atendimento, uma fila por nível de prioridade;
 *  - Pilha<Operacao>: histórico recente de operações e desfazimento (LIFO);
 *  - LinkedList<Solicitacao>: cadastro geral de solicitações (lista simples);
 *  - DLL<Atendimento>: histórico navegável dos atendimentos concluídos.
 *
 * Fluxo: nova solicitação -> cadastro geral (lista) -> fila da prioridade ->
 * atender -> pilha de operações e lista dupla de atendimentos concluídos.
 *
 * Nenhuma classe da Java Collections Framework é utilizada.
 */
public class CentralAtendimento {

    private final ControleFilas filas = new ControleFilas();
    private final LinkedList<Solicitacao> cadastro = new LinkedList<Solicitacao>();
    // O parâmetro do construtor da Pilha do professor é ignorado: a pilha
    // encadeada não possui capacidade máxima.
    private final Pilha<Operacao> historico = new Pilha<Operacao>(0);
    private final DLL<Atendimento> atendimentos = new DLL<Atendimento>();

    private int proximoCodigo = 101;     // os códigos começam em 101, como no enunciado
    private int proximaSequencia = 1;    // ordem cronológica dos atendimentos concluídos

    // Posição atual na navegação do histórico de atendimentos (null = inativa).
    private NodeDLL<Atendimento> cursorHistorico = null;

    // ==================================================================
    // ATENDIMENTO
    // ==================================================================

    /** Cadastra uma solicitação: lista simples, fila da prioridade e pilha. */
    public Solicitacao cadastrar(String solicitante, String descricao,
                                 String categoria, int prioridade)
            throws CampoInvalidoException, PrioridadeInvalidaException {
        validarTexto("Nome do solicitante", solicitante);
        validarTexto("Descrição do problema", descricao);
        validarTexto("Categoria", categoria);
        if (prioridade != Solicitacao.PRIORIDADE_ALTA
                && prioridade != Solicitacao.PRIORIDADE_BAIXA) {
            throw new PrioridadeInvalidaException("Prioridade inválida: " + prioridade
                    + ". Use 1 (alta) ou 2 (baixa).");
        }

        Solicitacao s = new Solicitacao(proximoCodigo, solicitante.trim(),
                descricao.trim(), categoria.trim().toUpperCase(), prioridade);

        if (!cadastro.addLast(s)) {
            throw new EstruturaInconsistenteException("Falha ao inserir no cadastro geral.");
        }
        filas.inserir(s);
        empilhar(new Operacao(Operacao.CADASTRO, s));
        proximoCodigo++;
        return s;
    }

    /** Próxima solicitação global (sem removê-la). */
    public Solicitacao consultarProxima() throws SemSolicitacaoException {
        return filas.proxima();
    }

    /** Atende a próxima solicitação global. */
    public Solicitacao atenderProxima(String responsavel)
            throws CampoInvalidoException, SemSolicitacaoException {
        validarTexto("Responsável pelo atendimento", responsavel);

        int contadorAnterior = filas.getAltaSeguidas();
        Solicitacao s = filas.atender();
        s.setResponsavel(responsavel.trim());
        s.setStatus(Solicitacao.CONCLUIDA);

        Atendimento a = new Atendimento(proximaSequencia++, s, responsavel.trim());
        if (!atendimentos.insertAscending(a)) {
            throw new EstruturaInconsistenteException("Falha ao inserir no histórico de atendimentos.");
        }
        empilhar(new Operacao(Operacao.ATENDIMENTO, s, a, contadorAnterior));
        return s;
    }

    /** Texto com as duas filas, exibidas separadamente. */
    public String exibirFilas() {
        try {
            return "--- Fila de prioridade 1 (alta) ---\n" + filas.formatar(Solicitacao.PRIORIDADE_ALTA)
                    + "--- Fila de prioridade 2 (baixa) ---\n" + filas.formatar(Solicitacao.PRIORIDADE_BAIXA);
        } catch (PrioridadeInvalidaException e) {
            throw new EstruturaInconsistenteException("Prioridade interna inválida.", e);
        }
    }

    /** Texto com a fila de uma única prioridade. */
    public String exibirFila(int prioridade) throws PrioridadeInvalidaException {
        return filas.formatar(prioridade);
    }

    public int quantidadeAguardando() {
        return filas.total();
    }

    public int quantidadeAguardando(int prioridade) throws PrioridadeInvalidaException {
        return filas.quantidade(prioridade);
    }

    public String demonstrarSelecao() {
        return filas.descreverSelecao();
    }

    // ==================================================================
    // OPERAÇÕES (Pilha)
    // ==================================================================

    public Operacao ultimaOperacao() throws SemRegistroException {
        if (historico.isEmpty()) {
            throw new SemRegistroException("Nenhuma operação foi realizada ainda.");
        }
        return topo();
    }

    public int quantidadeOperacoes() {
        return historico.sizeElements();
    }

    /** Histórico recente, da operação mais recente para a mais antiga (LIFO). */
    public String historicoRecente() throws SemRegistroException {
        if (historico.isEmpty()) {
            throw new SemRegistroException("Nenhuma operação foi registrada ainda.");
        }
        Pilha<Operacao> auxiliar = new Pilha<Operacao>(0);
        StringBuilder sb = new StringBuilder();
        while (!historico.isEmpty()) {            // primeira inversão
            Operacao op = desempilhar(historico);
            sb.append("  ").append(op).append("\n");
            empilhar(auxiliar, op);
        }
        while (!auxiliar.isEmpty()) {             // segunda inversão: restaura a ordem
            empilhar(historico, desempilhar(auxiliar));
        }
        return sb.toString();
    }

    /**
     * Desfaz o último atendimento. Somente operações do tipo ATENDIMENTO podem
     * ser desfeitas. A solicitação retorna ao INÍCIO da fila de sua prioridade
     * (posição que ocupava antes), o contador de anti-inanição é restaurado e o
     * registro correspondente é removido do histórico de atendimentos.
     */
    public Solicitacao desfazerUltimaOperacao()
            throws SemRegistroException, OperacaoInvalidaException {
        if (historico.isEmpty()) {
            throw new SemRegistroException("Não há operações para desfazer.");
        }
        Operacao op = topo();
        if (!Operacao.ATENDIMENTO.equals(op.getTipo())) {
            throw new OperacaoInvalidaException("A última operação foi um " + op.getTipo()
                    + ". Somente operações de ATENDIMENTO podem ser desfeitas.");
        }

        Solicitacao s = op.getSolicitacao();
        Atendimento a = op.getAtendimento();

        if (cursorHistorico != null && a.equals(cursorHistorico.getData())) {
            cursorHistorico = null;               // a navegação perde a referência
        }
        if (!atendimentos.remove(a)) {
            throw new EstruturaInconsistenteException("Atendimento ausente do histórico.");
        }
        s.setStatus(Solicitacao.AGUARDANDO);
        s.setResponsavel("-");
        filas.devolverAoInicio(s, op.getContadorAnterior());
        desempilhar(historico);
        return s;
    }

    // ==================================================================
    // CADASTRO GERAL (Lista Simples)
    // ==================================================================

    public String exibirTodas() throws SemRegistroException {
        if (cadastro.isEmpty()) {
            throw new SemRegistroException("O cadastro geral está vazio.");
        }
        StringBuilder sb = new StringBuilder();
        Node<Solicitacao> atual = cadastro.getHead();
        while (atual != null) {
            sb.append("  ").append(atual.getDado()).append("\n");
            atual = atual.getProx();
        }
        return sb.toString();
    }

    public Solicitacao pesquisarPorCodigo(int codigo) throws SolicitacaoNaoEncontradaException {
        // A busca da LinkedList compara com equals(); a Solicitacao considera
        // iguais os códigos idênticos, então basta uma chave com o código desejado.
        Node<Solicitacao> no = cadastro.search(chaveDeBusca(codigo));
        if (no == null) {
            throw new SolicitacaoNaoEncontradaException(
                    "Não existe solicitação com o código " + codigo + ".");
        }
        return no.getDado();
    }

    public String exibirPorStatus(String status) throws CampoInvalidoException {
        String chave = status == null ? "" : status.trim().toUpperCase();
        if (!Solicitacao.statusValido(chave)) {
            throw new CampoInvalidoException("Status inválido: \"" + status
                    + "\". Use AGUARDANDO, EM_ATENDIMENTO, CONCLUIDA ou CANCELADA.");
        }
        StringBuilder sb = new StringBuilder();
        Node<Solicitacao> atual = cadastro.getHead();
        while (atual != null) {
            if (atual.getDado().getStatus().equals(chave)) {
                sb.append("  ").append(atual.getDado()).append("\n");
            }
            atual = atual.getProx();
        }
        return sb.length() == 0 ? "  (nenhuma solicitação com o status " + chave + ")\n" : sb.toString();
    }

    public String exibirPorCategoria(String categoria) throws CampoInvalidoException {
        validarTexto("Categoria", categoria);
        String chave = categoria.trim();
        StringBuilder sb = new StringBuilder();
        Node<Solicitacao> atual = cadastro.getHead();
        while (atual != null) {
            if (atual.getDado().getCategoria().equalsIgnoreCase(chave)) {
                sb.append("  ").append(atual.getDado()).append("\n");
            }
            atual = atual.getProx();
        }
        return sb.length() == 0 ? "  (nenhuma solicitação na categoria " + chave.toUpperCase() + ")\n" : sb.toString();
    }

    /**
     * Cancela uma solicitação que ainda aguarda atendimento: sai da fila, o
     * status passa a CANCELADA e ela permanece no cadastro geral.
     */
    public Solicitacao cancelar(int codigo)
            throws SolicitacaoNaoEncontradaException, OperacaoInvalidaException {
        Solicitacao s = pesquisarPorCodigo(codigo);
        if (!Solicitacao.AGUARDANDO.equals(s.getStatus())) {
            throw new OperacaoInvalidaException("Só é possível cancelar solicitações "
                    + "AGUARDANDO. A solicitação #" + codigo + " está " + s.getStatus() + ".");
        }
        if (!filas.remover(s)) {
            throw new EstruturaInconsistenteException("Solicitação aguardando fora da fila.");
        }
        s.setStatus(Solicitacao.CANCELADA);
        empilhar(new Operacao(Operacao.CANCELAMENTO, s));
        return s;
    }

    /**
     * Remove uma solicitação do cadastro geral. Se estiver aguardando, também
     * sai da fila. Solicitações CONCLUIDAS não podem ser removidas, pois fazem
     * parte do histórico de atendimentos.
     */
    public Solicitacao remover(int codigo)
            throws SolicitacaoNaoEncontradaException, OperacaoInvalidaException {
        Solicitacao s = pesquisarPorCodigo(codigo);
        if (Solicitacao.CONCLUIDA.equals(s.getStatus())) {
            throw new OperacaoInvalidaException("A solicitação #" + codigo
                    + " está CONCLUIDA e faz parte do histórico de atendimentos; não pode ser removida.");
        }
        if (Solicitacao.AGUARDANDO.equals(s.getStatus()) && !filas.remover(s)) {
            throw new EstruturaInconsistenteException("Solicitação aguardando fora da fila.");
        }
        cadastro.remove(s);
        empilhar(new Operacao(Operacao.REMOCAO, s));
        return s;
    }

    public int quantidadeNoCadastro() {
        return cadastro.getSize();
    }

    public int quantidadePorStatus(String status) {
        int total = 0;
        Node<Solicitacao> atual = cadastro.getHead();
        while (atual != null) {
            if (atual.getDado().getStatus().equals(status)) {
                total++;
            }
            atual = atual.getProx();
        }
        return total;
    }

    // ==================================================================
    // HISTÓRICO DE ATENDIMENTOS (Lista Duplamente Encadeada)
    // ==================================================================

    public int quantidadeAtendimentos() {
        return atendimentos.getSize();
    }

    /** Percurso do início para o fim (do atendimento mais antigo ao mais recente). */
    public String historicoDoPrimeiroAoUltimo() throws SemRegistroException {
        exigirHistoricoNaoVazio();
        StringBuilder sb = new StringBuilder();
        NodeDLL<Atendimento> atual = atendimentos.getHead();
        while (atual != null) {
            sb.append("  ").append(atual.getData()).append("\n");
            atual = atual.getRight();
        }
        return sb.toString();
    }

    /** Percurso do fim para o início, usando a referência ao nó anterior. */
    public String historicoDoUltimoAoPrimeiro() throws SemRegistroException {
        exigirHistoricoNaoVazio();
        StringBuilder sb = new StringBuilder();
        NodeDLL<Atendimento> atual = ultimoNo();
        while (atual != null) {
            sb.append("  ").append(atual.getData()).append("\n");
            atual = atual.getLeft();
        }
        return sb.toString();
    }

    public Atendimento primeiroAtendimento() throws SemRegistroException {
        exigirHistoricoNaoVazio();
        return atendimentos.getHead().getData();
    }

    public Atendimento ultimoAtendimento() throws SemRegistroException {
        exigirHistoricoNaoVazio();
        return ultimoNo().getData();
    }

    /** Localiza o atendimento concluído de uma solicitação, pelo código dela. */
    public Atendimento pesquisarAtendimento(int codigoSolicitacao) throws SemRegistroException {
        exigirHistoricoNaoVazio();
        NodeDLL<Atendimento> atual = atendimentos.getHead();
        while (atual != null) {
            if (atual.getData().getSolicitacao().getCodigo() == codigoSolicitacao) {
                return atual.getData();
            }
            atual = atual.getRight();
        }
        throw new SemRegistroException("Não há atendimento concluído para a solicitação #"
                + codigoSolicitacao + ".");
    }

    // --- Navegação (anterior / próximo) ---

    /** Inicia a navegação no atendimento mais recente. */
    public Atendimento iniciarNavegacao() throws SemRegistroException {
        exigirHistoricoNaoVazio();
        cursorHistorico = ultimoNo();
        return cursorHistorico.getData();
    }

    public Atendimento atualNaNavegacao() throws OperacaoInvalidaException {
        exigirNavegacaoAtiva();
        return cursorHistorico.getData();
    }

    public Atendimento irParaAnterior() throws OperacaoInvalidaException {
        exigirNavegacaoAtiva();
        if (cursorHistorico.getLeft() == null) {
            throw new OperacaoInvalidaException("Já está no primeiro atendimento do histórico.");
        }
        cursorHistorico = cursorHistorico.getLeft();
        return cursorHistorico.getData();
    }

    public Atendimento irParaProximo() throws OperacaoInvalidaException {
        exigirNavegacaoAtiva();
        if (cursorHistorico.getRight() == null) {
            throw new OperacaoInvalidaException("Já está no último atendimento do histórico.");
        }
        cursorHistorico = cursorHistorico.getRight();
        return cursorHistorico.getData();
    }

    public Atendimento irParaPrimeiro() throws OperacaoInvalidaException {
        exigirNavegacaoAtiva();
        cursorHistorico = atendimentos.getHead();
        return cursorHistorico.getData();
    }

    public Atendimento irParaUltimo() throws OperacaoInvalidaException {
        exigirNavegacaoAtiva();
        cursorHistorico = ultimoNo();
        return cursorHistorico.getData();
    }

    public void encerrarNavegacao() {
        cursorHistorico = null;
    }

    // ==================================================================
    // RELATÓRIO
    // ==================================================================

    public String resumo() {
        StringBuilder sb = new StringBuilder();
        sb.append("  Solicitações no cadastro geral ..: ").append(cadastro.getSize()).append("\n");
        sb.append("    AGUARDANDO ....................: ").append(quantidadePorStatus(Solicitacao.AGUARDANDO)).append("\n");
        sb.append("    CONCLUIDA .....................: ").append(quantidadePorStatus(Solicitacao.CONCLUIDA)).append("\n");
        sb.append("    CANCELADA .....................: ").append(quantidadePorStatus(Solicitacao.CANCELADA)).append("\n");
        sb.append("  Aguardando na fila de prioridade 1: ").append(quantidadeFila(Solicitacao.PRIORIDADE_ALTA)).append("\n");
        sb.append("  Aguardando na fila de prioridade 2: ").append(quantidadeFila(Solicitacao.PRIORIDADE_BAIXA)).append("\n");
        sb.append("  Atendimentos concluídos (histórico): ").append(atendimentos.getSize()).append("\n");
        sb.append("  Operações no histórico recente ..: ").append(historico.sizeElements());
        return sb.toString();
    }

    // ==================================================================
    // MÉTODOS AUXILIARES
    // ==================================================================

    private int quantidadeFila(int prioridade) {
        try {
            return filas.quantidade(prioridade);
        } catch (PrioridadeInvalidaException e) {
            throw new EstruturaInconsistenteException("Prioridade interna inválida.", e);
        }
    }

    private void validarTexto(String campo, String valor) throws CampoInvalidoException {
        if (valor == null || valor.trim().isEmpty()) {
            throw new CampoInvalidoException(campo + " não pode ficar vazio.");
        }
    }

    // Solicitação usada somente como chave de comparação na busca por código.
    private Solicitacao chaveDeBusca(int codigo) {
        return new Solicitacao(codigo, "-", "-", "-", Solicitacao.PRIORIDADE_ALTA);
    }

    private void exigirHistoricoNaoVazio() throws SemRegistroException {
        if (atendimentos.isEmpty()) {
            throw new SemRegistroException("O histórico de atendimentos está vazio.");
        }
    }

    private void exigirNavegacaoAtiva() throws OperacaoInvalidaException {
        if (cursorHistorico == null) {
            throw new OperacaoInvalidaException("A navegação no histórico não foi iniciada.");
        }
    }

    // Percorre a lista até o último nó, usando a referência para o próximo.
    private NodeDLL<Atendimento> ultimoNo() {
        NodeDLL<Atendimento> atual = atendimentos.getHead();
        while (atual != null && atual.getRight() != null) {
            atual = atual.getRight();
        }
        return atual;
    }

    // As classes do professor declaram "throws Exception". Os métodos abaixo
    // convertem uma falha inesperada em EstruturaInconsistenteException.

    private void empilhar(Operacao op) {
        empilhar(historico, op);
    }

    private void empilhar(Pilha<Operacao> pilha, Operacao op) {
        try {
            pilha.push(op);
        } catch (Exception e) {
            throw new EstruturaInconsistenteException("Falha ao empilhar a operação.", e);
        }
    }

    private Operacao desempilhar(Pilha<Operacao> pilha) {
        try {
            return pilha.pop();
        } catch (Exception e) {
            throw new EstruturaInconsistenteException("Falha ao desempilhar a operação.", e);
        }
    }

    private Operacao topo() {
        try {
            return historico.topo();
        } catch (Exception e) {
            throw new EstruturaInconsistenteException("Falha ao consultar o topo da pilha.", e);
        }
    }
}
