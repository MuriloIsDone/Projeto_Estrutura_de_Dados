package testes;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Atendimento;
import model.Operacao;
import model.Solicitacao;
import service.CentralAtendimento;

/**
 * Testes automatizados do 2º bimestre (sem bibliotecas externas).
 *
 * Cada caso cria uma CentralAtendimento nova, executa um cenário e compara o
 * resultado obtido com o esperado. O resultado é impresso no console e, se for
 * informado um caminho como argumento, gravado em um arquivo de texto com
 * colunas separadas por tabulação (usado para montar o relatório).
 *
 * Execução: java -cp bin testes.TestesB2 [arquivo_de_saida.tsv]
 */
public class TestesB2 {

    // ---------------------------------------------------------------- infraestrutura

    private interface Teste {
        Resultado executar() throws Exception;
    }

    private interface Acao {
        void executar() throws Exception;
    }

    private static class Resultado {
        final boolean ok;
        final String obtido;

        Resultado(boolean ok, String obtido) {
            this.ok = ok;
            this.obtido = obtido;
        }
    }

    private static final int MAXIMO = 200;
    private static final String[][] linhas = new String[MAXIMO][7];
    private static int total = 0;
    private static int aprovados = 0;

    private static Resultado r(boolean ok, String obtido) {
        return new Resultado(ok, obtido);
    }

    private static void caso(String id, String grupo, String situacao, String entrada,
                             String esperado, Teste teste) {
        Resultado res;
        try {
            res = teste.executar();
        } catch (Exception e) {
            res = new Resultado(false, "Exceção inesperada: " + e);
        }
        String status = res.ok ? "Aprovado" : "REPROVADO";
        linhas[total][0] = id;
        linhas[total][1] = grupo;
        linhas[total][2] = situacao;
        linhas[total][3] = entrada;
        linhas[total][4] = esperado;
        linhas[total][5] = res.obtido;
        linhas[total][6] = status;
        total++;
        if (res.ok) {
            aprovados++;
        }
        System.out.println("[" + status + "] " + id + " (" + grupo + ") " + situacao);
        if (!res.ok) {
            System.out.println("          esperado: " + esperado);
            System.out.println("          obtido  : " + res.obtido);
        }
    }

    // Executa a ação e devolve "NomeDaExceção: mensagem" ou "nenhuma exceção".
    private static String capturar(Acao acao) {
        try {
            acao.executar();
            return "nenhuma exceção";
        } catch (Exception e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    // Extrai, na ordem em que aparecem, os códigos "#NNN" de um texto.
    private static String codigos(String texto) {
        Matcher m = Pattern.compile("#(\\d+)").matcher(texto);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(m.group(1));
        }
        return sb.toString();
    }

    private static Solicitacao cad(CentralAtendimento c, String nome, int prioridade) throws Exception {
        return c.cadastrar(nome, "Problema de " + nome, "REDE", prioridade);
    }

    // Atende tudo e devolve a ordem de atendimento (códigos separados por vírgula).
    private static String atenderTodas(CentralAtendimento c) throws Exception {
        StringBuilder sb = new StringBuilder();
        while (c.quantidadeAguardando() > 0) {
            Solicitacao s = c.atenderProxima("Técnico");
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s.getCodigo());
        }
        return sb.toString();
    }

    private static boolean contem(String texto, String trecho) {
        return texto != null && texto.contains(trecho);
    }

    // ---------------------------------------------------------------- principal

    public static void main(String[] args) throws IOException {
        System.out.println("=== TESTES AUTOMATIZADOS - 2º BIMESTRE ===");
        regressao();
        filas();
        prioridades();
        cancelamentoRemocao();
        desfazer();
        pilha();
        listaSimples();
        listaDupla();
        validacao();
        integracao();

        System.out.println();
        System.out.println("Total de testes: " + total);
        System.out.println("Aprovados......: " + aprovados);
        System.out.println("Reprovados.....: " + (total - aprovados));

        if (args.length > 0) {
            try (PrintWriter out = new PrintWriter(new FileWriter(args[0]))) {
                for (int i = 0; i < total; i++) {
                    out.println(String.join("\t", linhas[i]));
                }
            }
        }
        if (aprovados != total) {
            System.exit(1);
        }
    }

    // ---------------------------------------------------------------- 1º bimestre

    private static void regressao() {
        String g = "Regressão (1º bim.)";

        caso("RG01", g, "Ordem FIFO do 1º bimestre (mesma prioridade)",
                "Cadastrar Ana, Bruno e Carla (prioridade 1) e atender todas",
                "Ordem de atendimento 101,102,103",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    String ordem = atenderTodas(c);
                    return r(ordem.equals("101,102,103"), "Ordem de atendimento " + ordem);
                });

        caso("RG02", g, "Consultar e atender com a fila vazia",
                "Sistema recém-iniciado: opções 2 e 3",
                "SemSolicitacaoException nas duas operações, sem travar",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String a = capturar(() -> c.consultarProxima());
                    String b = capturar(() -> c.atenderProxima("Técnico"));
                    return r(a.startsWith("SemSolicitacaoException") && b.startsWith("SemSolicitacaoException"),
                            a + " | " + b);
                });

        caso("RG03", g, "Última operação e desfazer com a pilha vazia",
                "Sistema recém-iniciado: opções 6 e 8",
                "SemRegistroException nas duas operações",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String a = capturar(() -> c.ultimaOperacao());
                    String b = capturar(() -> c.desfazerUltimaOperacao());
                    return r(a.startsWith("SemRegistroException") && b.startsWith("SemRegistroException"),
                            a + " | " + b);
                });

        caso("RG04", g, "Histórico em ordem LIFO (exemplo do item 11 do enunciado)",
                "101 cadastrada, 102 cadastrada, 101 atendida, 103 cadastrada, 102 atendida",
                "Do topo à base: ATENDIMENTO 102, CADASTRO 103, ATENDIMENTO 101, CADASTRO 102, CADASTRO 101",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1);
                    c.atenderProxima("Técnico");
                    cad(c, "Carla", 1);
                    c.atenderProxima("Técnico");
                    String h = c.historicoRecente();
                    String[] linhasH = h.trim().split("\n");
                    boolean ok = linhasH.length == 5
                            && linhasH[0].contains("ATENDIMENTO") && linhasH[0].contains("#102")
                            && linhasH[1].contains("CADASTRO") && linhasH[1].contains("#103")
                            && linhasH[2].contains("ATENDIMENTO") && linhasH[2].contains("#101")
                            && linhasH[3].contains("CADASTRO") && linhasH[3].contains("#102")
                            && linhasH[4].contains("CADASTRO") && linhasH[4].contains("#101");
                    return r(ok, "Ordem (topo -> base): " + codigos(h) + " com tipos "
                            + tipos(linhasH));
                });

        caso("RG05", g, "Desfazer atendimento devolve a solicitação ao início da fila",
                "Cadastrar 101, 102; atender 101; desfazer",
                "Próxima = #101 com status AGUARDANDO",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1);
                    c.atenderProxima("Técnico");
                    Solicitacao s = c.desfazerUltimaOperacao();
                    Solicitacao prox = c.consultarProxima();
                    return r(prox.getCodigo() == 101 && Solicitacao.AGUARDANDO.equals(s.getStatus()),
                            "Próxima = #" + prox.getCodigo() + ", status " + s.getStatus());
                });

        caso("RG06", g, "Desfazer quando a última operação não é um atendimento",
                "Cadastrar uma solicitação e desfazer",
                "OperacaoInvalidaException informando que é um CADASTRO; nada é alterado",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    String e = capturar(() -> c.desfazerUltimaOperacao());
                    boolean intacto = c.quantidadeAguardando() == 1 && c.quantidadeOperacoes() == 1;
                    return r(e.startsWith("OperacaoInvalidaException") && e.contains("CADASTRO") && intacto,
                            e + " | aguardando=" + c.quantidadeAguardando());
                });

        caso("RG07", g, "Consultar a próxima não remove da fila",
                "Cadastrar 2 solicitações; consultar a próxima duas vezes",
                "Mesma solicitação nas duas consultas e quantidade inalterada (2)",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1);
                    int a = c.consultarProxima().getCodigo();
                    int b = c.consultarProxima().getCodigo();
                    return r(a == 101 && b == 101 && c.quantidadeAguardando() == 2,
                            "Consultas: #" + a + " e #" + b + "; aguardando=" + c.quantidadeAguardando());
                });
    }

    private static String tipos(String[] linhasH) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < linhasH.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(linhasH[i].contains("ATENDIMENTO") ? "ATD" : "CAD");
        }
        return sb.toString();
    }

    // ---------------------------------------------------------------- Fila encadeada

    private static void filas() {
        String g = "Fila encadeada";

        caso("F01", g, "Inserção do primeiro e único elemento",
                "Cadastrar uma solicitação (prioridade 1)",
                "Próxima = #101; 1 aguardando",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    return r(c.consultarProxima().getCodigo() == 101 && c.quantidadeAguardando() == 1,
                            "Próxima = #" + c.consultarProxima().getCodigo() + "; aguardando=" + c.quantidadeAguardando());
                });

        caso("F02", g, "FIFO na fila de baixa prioridade",
                "Cadastrar 3 solicitações de prioridade 2 e atender todas",
                "Ordem 101,102,103",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 2); cad(c, "Bruno", 2); cad(c, "Carla", 2);
                    String ordem = atenderTodas(c);
                    return r(ordem.equals("101,102,103"), "Ordem " + ordem);
                });

        caso("F03", g, "Esvaziamento completo e nova tentativa de atender",
                "Cadastrar 2, atender 2 e tentar atender novamente",
                "Fila fica com 0 e a nova tentativa lança SemSolicitacaoException",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1);
                    c.atenderProxima("Técnico"); c.atenderProxima("Técnico");
                    String e = capturar(() -> c.atenderProxima("Técnico"));
                    return r(c.quantidadeAguardando() == 0 && e.startsWith("SemSolicitacaoException"),
                            "aguardando=" + c.quantidadeAguardando() + " | " + e);
                });

        caso("F04", g, "Exibir a fila não altera a ordem dos elementos",
                "Cadastrar 4 solicitações; exibir as filas duas vezes e atender",
                "As duas exibições são idênticas e a ordem de atendimento continua 101,102,103,104",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1); cad(c, "Davi", 1);
                    String a = codigos(c.exibirFilas());
                    String b = codigos(c.exibirFilas());
                    String ordem = atenderTodas(c);
                    return r(a.equals(b) && ordem.equals("101,102,103,104"),
                            "Exibições: " + a + " e " + b + "; atendimento: " + ordem);
                });

        caso("F05", g, "Estrutura sem limite fixo de capacidade (1000 solicitações)",
                "Cadastrar 1000 solicitações e atender todas",
                "Nenhuma recusa; primeira atendida #101, última #1100; fila vazia no final",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    for (int i = 0; i < 1000; i++) {
                        cad(c, "Cliente" + i, 1);
                    }
                    int aguardando = c.quantidadeAguardando();
                    int primeira = c.atenderProxima("Técnico").getCodigo();
                    int ultima = primeira;
                    while (c.quantidadeAguardando() > 0) {
                        ultima = c.atenderProxima("Técnico").getCodigo();
                    }
                    return r(aguardando == 1000 && primeira == 101 && ultima == 1100
                            && c.quantidadeAguardando() == 0,
                            "aguardando=" + aguardando + "; primeira=#" + primeira + "; última=#" + ultima
                            + "; final=" + c.quantidadeAguardando());
                });
    }

    // ---------------------------------------------------------------- Prioridades

    private static void prioridades() {
        String g = "Prioridades";

        caso("P01", g, "Prioridades distintas (equivalente ao exemplo do enunciado)",
                "Cadastrar P1,P2,P1,P2 (códigos 101 a 104) e atender todas",
                "Ordem 101,103,102,104 (fila 1 esvazia antes da fila 2)",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 2); cad(c, "Carla", 1); cad(c, "Davi", 2);
                    String ordem = atenderTodas(c);
                    return r(ordem.equals("101,103,102,104"), "Ordem " + ordem);
                });

        caso("P02", g, "Fila prioritária vazia: atende a de baixa prioridade",
                "Cadastrar apenas solicitações de prioridade 2 e atender",
                "A solicitação #101 (prioridade 2) é atendida normalmente",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 2); cad(c, "Bruno", 2);
                    Solicitacao s = c.atenderProxima("Técnico");
                    return r(s.getCodigo() == 101 && s.getPrioridade() == 2,
                            "Atendida #" + s.getCodigo() + " (prioridade " + s.getPrioridade() + ")");
                });

        caso("P03", g, "Fila de baixa prioridade vazia: atende a de alta prioridade",
                "Cadastrar apenas solicitações de prioridade 1 e atender",
                "A solicitação #101 (prioridade 1) é atendida normalmente",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    Solicitacao s = c.atenderProxima("Técnico");
                    return r(s.getCodigo() == 101 && s.getPrioridade() == 1,
                            "Atendida #" + s.getCodigo() + " (prioridade " + s.getPrioridade() + ")");
                });

        caso("P04", g, "Todas as filas vazias",
                "Consultar e atender sem nenhuma solicitação; consultar quantidades",
                "SemSolicitacaoException nas duas operações; quantidades 0, 0 e total 0",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String a = capturar(() -> c.consultarProxima());
                    String b = capturar(() -> c.atenderProxima("Técnico"));
                    boolean ok = a.startsWith("SemSolicitacaoException")
                            && b.startsWith("SemSolicitacaoException")
                            && c.quantidadeAguardando(1) == 0 && c.quantidadeAguardando(2) == 0
                            && c.quantidadeAguardando() == 0;
                    return r(ok, a + " | quantidades " + c.quantidadeAguardando(1) + ", "
                            + c.quantidadeAguardando(2) + ", total " + c.quantidadeAguardando());
                });

        caso("P05", g, "Prioridades inválidas (0, 3 e -5)",
                "Tentar cadastrar com prioridade 0, 3 e -5",
                "PrioridadeInvalidaException nas três tentativas e nada é cadastrado",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String a = capturar(() -> c.cadastrar("Ana", "Problema", "REDE", 0));
                    String b = capturar(() -> c.cadastrar("Ana", "Problema", "REDE", 3));
                    String d = capturar(() -> c.cadastrar("Ana", "Problema", "REDE", -5));
                    boolean ok = a.startsWith("PrioridadeInvalidaException")
                            && b.startsWith("PrioridadeInvalidaException")
                            && d.startsWith("PrioridadeInvalidaException")
                            && c.quantidadeNoCadastro() == 0 && c.quantidadeAguardando() == 0
                            && c.quantidadeOperacoes() == 0;
                    return r(ok, a + " | cadastro=" + c.quantidadeNoCadastro()
                            + ", fila=" + c.quantidadeAguardando() + ", operações=" + c.quantidadeOperacoes());
                });

        caso("P06", g, "Anti-inanição: 3 da fila alta, 1 da baixa",
                "5 de prioridade 1 (101 a 105) e 2 de prioridade 2 (106 e 107); atender todas",
                "Ordem 101,102,103,106,104,105,107",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    for (int i = 0; i < 5; i++) {
                        cad(c, "Alta" + i, 1);
                    }
                    cad(c, "Baixa1", 2); cad(c, "Baixa2", 2);
                    String ordem = atenderTodas(c);
                    return r(ordem.equals("101,102,103,106,104,105,107"), "Ordem " + ordem);
                });

        caso("P07", g, "Novas entradas durante os atendimentos",
                "Cadastrar P1 (101) e P2 (102); atender; cadastrar P1 (103); atender todas",
                "Ordem 101,103,102 (a nova prioridade 1 passa à frente da 2)",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 2);
                    StringBuilder ordem = new StringBuilder();
                    ordem.append(c.atenderProxima("Técnico").getCodigo()).append(",");
                    cad(c, "Carla", 1);
                    ordem.append(atenderTodas(c));
                    return r(ordem.toString().equals("101,103,102"), "Ordem " + ordem);
                });

        caso("P08", g, "Contador de anti-inanição só conta se a fila baixa está esperando",
                "Atender 5 solicitações de prioridade 1 sem nenhuma de prioridade 2; depois cadastrar P2 (106) e P1 (107)",
                "A próxima é #107 (prioridade 1), pois ninguém da fila baixa esperava antes",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    for (int i = 0; i < 5; i++) {
                        cad(c, "Alta" + i, 1);
                    }
                    atenderTodas(c);
                    cad(c, "Baixa", 2); cad(c, "AltaNova", 1);
                    int prox = c.consultarProxima().getCodigo();
                    return r(prox == 107, "Próxima = #" + prox);
                });

        caso("P09", g, "Demonstração da seleção (opção 21)",
                "4 de prioridade 1 e 1 de prioridade 2; atender 3 e demonstrar a seleção",
                "Exibe 3 de 3 atendimentos seguidos e indica a fila BAIXA com a #105",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    for (int i = 0; i < 4; i++) {
                        cad(c, "Alta" + i, 1);
                    }
                    cad(c, "Baixa", 2);
                    for (int i = 0; i < 3; i++) {
                        c.atenderProxima("Técnico");
                    }
                    String t = c.demonstrarSelecao();
                    boolean ok = contem(t, "Atendimentos seguidos da fila alta: 3 de 3")
                            && contem(t, "fila BAIXA") && contem(t, "#105");
                    return r(ok, "3 de 3 = " + contem(t, "3 de 3") + "; BAIXA = " + contem(t, "fila BAIXA")
                            + "; próxima #105 = " + contem(t, "#105"));
                });

        caso("P10", g, "Quantidades por prioridade e total geral",
                "Cadastrar 3 de prioridade 1 e 2 de prioridade 2; atender 1",
                "Prioridade 1 = 2, prioridade 2 = 2, total = 4",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "A", 1); cad(c, "B", 1); cad(c, "C", 1); cad(c, "D", 2); cad(c, "E", 2);
                    c.atenderProxima("Técnico");
                    int p1 = c.quantidadeAguardando(1);
                    int p2 = c.quantidadeAguardando(2);
                    int t = c.quantidadeAguardando();
                    return r(p1 == 2 && p2 == 2 && t == 4, "P1=" + p1 + ", P2=" + p2 + ", total=" + t);
                });
    }

    // ---------------------------------------------------------------- Cancelamento e remoção

    private static void cancelamentoRemocao() {
        String g = "Cancelamento e remoção";

        caso("C01", g, "Cancelar solicitação aguardando preserva a ordem das demais",
                "Cadastrar 101, 102, 103 (prioridade 1); cancelar #102; atender todas",
                "Ordem 101,103; #102 permanece no cadastro com status CANCELADA",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    Solicitacao cancelada = c.cancelar(102);
                    String ordem = atenderTodas(c);
                    boolean ok = ordem.equals("101,103")
                            && Solicitacao.CANCELADA.equals(cancelada.getStatus())
                            && c.quantidadeNoCadastro() == 3;
                    return r(ok, "Ordem " + ordem + "; #102 " + cancelada.getStatus()
                            + "; cadastro=" + c.quantidadeNoCadastro());
                });

        caso("C02", g, "Cancelar registra a operação CANCELAMENTO na pilha",
                "Cadastrar e cancelar #101; consultar a última operação",
                "Topo da pilha: CANCELAMENTO da #101",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    c.cancelar(101);
                    Operacao op = c.ultimaOperacao();
                    return r(Operacao.CANCELAMENTO.equals(op.getTipo()) && op.getSolicitacao().getCodigo() == 101,
                            op.getTipo() + " da #" + op.getSolicitacao().getCodigo());
                });

        caso("C03", g, "Cancelar com código inexistente",
                "Cancelar #999",
                "SolicitacaoNaoEncontradaException",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    String e = capturar(() -> c.cancelar(999));
                    return r(e.startsWith("SolicitacaoNaoEncontradaException"), e);
                });

        caso("C04", g, "Cancelar duas vezes a mesma solicitação",
                "Cancelar #101 duas vezes",
                "Segunda tentativa lança OperacaoInvalidaException (já está CANCELADA)",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    c.cancelar(101);
                    String e = capturar(() -> c.cancelar(101));
                    return r(e.startsWith("OperacaoInvalidaException") && e.contains("CANCELADA"), e);
                });

        caso("C05", g, "Cancelar solicitação já concluída",
                "Atender #101 e tentar cancelá-la",
                "OperacaoInvalidaException (status CONCLUIDA)",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    c.atenderProxima("Técnico");
                    String e = capturar(() -> c.cancelar(101));
                    return r(e.startsWith("OperacaoInvalidaException") && e.contains("CONCLUIDA"), e);
                });

        caso("C06", g, "Cancelar a única solicitação da fila",
                "Cadastrar #101 e cancelá-la",
                "Fila fica vazia (0 aguardando) e consultar a próxima lança SemSolicitacaoException",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    c.cancelar(101);
                    String e = capturar(() -> c.consultarProxima());
                    return r(c.quantidadeAguardando() == 0 && e.startsWith("SemSolicitacaoException"),
                            "aguardando=" + c.quantidadeAguardando() + " | " + e);
                });

        caso("C07", g, "Remover solicitação aguardando: sai do cadastro e da fila",
                "Cadastrar 101, 102 e 103; remover #102",
                "Cadastro com 101,103; fila atende 101,103",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    c.remover(102);
                    String cadastro = codigos(c.exibirTodas());
                    String ordem = atenderTodas(c);
                    return r(cadastro.equals("101,103") && ordem.equals("101,103"),
                            "Cadastro " + cadastro + "; atendimento " + ordem);
                });

        caso("C08", g, "Remover solicitação cancelada",
                "Cancelar #101 e depois removê-la do cadastro",
                "Cadastro fica vazio",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    c.cancelar(101);
                    c.remover(101);
                    return r(c.quantidadeNoCadastro() == 0, "cadastro=" + c.quantidadeNoCadastro());
                });

        caso("C09", g, "Remover solicitação concluída é bloqueado",
                "Atender #101 e tentar removê-la",
                "OperacaoInvalidaException; a solicitação continua no cadastro e no histórico",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    c.atenderProxima("Técnico");
                    String e = capturar(() -> c.remover(101));
                    return r(e.startsWith("OperacaoInvalidaException") && c.quantidadeNoCadastro() == 1
                            && c.quantidadeAtendimentos() == 1, e);
                });

        caso("C10", g, "Remover com código inexistente",
                "Remover #999",
                "SolicitacaoNaoEncontradaException",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String e = capturar(() -> c.remover(999));
                    return r(e.startsWith("SolicitacaoNaoEncontradaException"), e);
                });
    }

    // ---------------------------------------------------------------- Desfazer

    private static void desfazer() {
        String g = "Desfazimento";

        caso("U01", g, "Desfazer devolve ao início da fila da mesma prioridade",
                "Cadastrar 101, 102, 103 (prioridade 1); atender 101; desfazer; atender todas",
                "Ordem 101,102,103",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    c.atenderProxima("Técnico");
                    c.desfazerUltimaOperacao();
                    String ordem = atenderTodas(c);
                    return r(ordem.equals("101,102,103"), "Ordem " + ordem);
                });

        caso("U02", g, "Desfazer atendimento da fila de baixa prioridade",
                "Cadastrar 101 e 102 (prioridade 2); atender 101; desfazer",
                "Próxima = #101 (início da fila baixa)",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 2); cad(c, "Bruno", 2);
                    c.atenderProxima("Técnico");
                    c.desfazerUltimaOperacao();
                    int prox = c.consultarProxima().getCodigo();
                    return r(prox == 101 && c.quantidadeAguardando(2) == 2, "Próxima = #" + prox
                            + "; fila baixa=" + c.quantidadeAguardando(2));
                });

        caso("U03", g, "Desfazer restaura o contador de anti-inanição",
                "4 de prioridade 1 e 1 de prioridade 2; atender 4 (a 4ª é a #105 da fila baixa); desfazer",
                "Próxima volta a ser #105 e o contador volta a 3 de 3",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    for (int i = 0; i < 4; i++) {
                        cad(c, "Alta" + i, 1);
                    }
                    cad(c, "Baixa", 2);
                    for (int i = 0; i < 4; i++) {
                        c.atenderProxima("Técnico");
                    }
                    c.desfazerUltimaOperacao();
                    int prox = c.consultarProxima().getCodigo();
                    String t = c.demonstrarSelecao();
                    return r(prox == 105 && contem(t, "3 de 3"), "Próxima = #" + prox
                            + "; contador 3 de 3 = " + contem(t, "3 de 3"));
                });

        caso("U04", g, "Desfazer duas vezes seguidas",
                "Cadastrar 101, 102, 103; atender 101 e 102; desfazer duas vezes",
                "Fila volta à ordem original 101,102,103",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    c.atenderProxima("Técnico"); c.atenderProxima("Técnico");
                    c.desfazerUltimaOperacao(); c.desfazerUltimaOperacao();
                    String fila = codigos(c.exibirFilas());
                    return r(fila.equals("101,102,103") && c.quantidadeAtendimentos() == 0,
                            "Fila " + fila + "; atendimentos=" + c.quantidadeAtendimentos());
                });

        caso("U05", g, "Desfazer remove o registro do histórico de atendimentos",
                "Atender #101 e #102; desfazer",
                "Histórico passa de 2 para 1 atendimento; o último passa a ser #101",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1);
                    c.atenderProxima("Técnico"); c.atenderProxima("Técnico");
                    int antes = c.quantidadeAtendimentos();
                    c.desfazerUltimaOperacao();
                    int depois = c.quantidadeAtendimentos();
                    int ultimo = c.ultimoAtendimento().getSolicitacao().getCodigo();
                    return r(antes == 2 && depois == 1 && ultimo == 101,
                            "antes=" + antes + ", depois=" + depois + ", último=#" + ultimo);
                });

        caso("U06", g, "Não desfaz após um cancelamento",
                "Cadastrar, atender e depois cancelar outra; desfazer",
                "OperacaoInvalidaException (última operação foi CANCELAMENTO); nada é alterado",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1);
                    c.atenderProxima("Técnico");
                    c.cancelar(102);
                    String e = capturar(() -> c.desfazerUltimaOperacao());
                    return r(e.startsWith("OperacaoInvalidaException") && e.contains("CANCELAMENTO")
                            && c.quantidadeAtendimentos() == 1, e);
                });

        caso("U07", g, "Desfazer mantém coerentes cadastro, fila, pilha e histórico",
                "Cadastrar 2; atender 1; desfazer",
                "Cadastro 2 (AGUARDANDO 2), fila 2, histórico de atendimentos 0, operações 2 (só os cadastros)",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1);
                    c.atenderProxima("Técnico");
                    c.desfazerUltimaOperacao();
                    boolean ok = c.quantidadeNoCadastro() == 2
                            && c.quantidadePorStatus(Solicitacao.AGUARDANDO) == 2
                            && c.quantidadeAguardando() == 2 && c.quantidadeAtendimentos() == 0
                            && c.quantidadeOperacoes() == 2;
                    return r(ok, "cadastro=" + c.quantidadeNoCadastro() + ", aguardando="
                            + c.quantidadePorStatus(Solicitacao.AGUARDANDO) + ", fila=" + c.quantidadeAguardando()
                            + ", atendimentos=" + c.quantidadeAtendimentos() + ", operações=" + c.quantidadeOperacoes());
                });
    }

    // ---------------------------------------------------------------- Pilha encadeada

    private static void pilha() {
        String g = "Pilha encadeada";

        caso("S01", g, "Pilha vazia: consultar a última operação",
                "Sistema recém-iniciado: opção 6",
                "SemRegistroException",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String e = capturar(() -> c.ultimaOperacao());
                    return r(e.startsWith("SemRegistroException"), e);
                });

        caso("S02", g, "Pilha com um único elemento",
                "Cadastrar uma solicitação e consultar a última operação",
                "Topo = CADASTRO da #101; 1 operação",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    Operacao op = c.ultimaOperacao();
                    return r(Operacao.CADASTRO.equals(op.getTipo()) && op.getSolicitacao().getCodigo() == 101
                            && c.quantidadeOperacoes() == 1,
                            op.getTipo() + " da #" + op.getSolicitacao().getCodigo()
                            + "; operações=" + c.quantidadeOperacoes());
                });

        caso("S03", g, "Consultar o topo não remove a operação",
                "Cadastrar 2 solicitações; consultar a última operação duas vezes",
                "Mesmo topo (#102) nas duas consultas e 2 operações na pilha",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1);
                    int a = c.ultimaOperacao().getSolicitacao().getCodigo();
                    int b = c.ultimaOperacao().getSolicitacao().getCodigo();
                    return r(a == 102 && b == 102 && c.quantidadeOperacoes() == 2,
                            "topo=#" + a + " e #" + b + "; operações=" + c.quantidadeOperacoes());
                });

        caso("S04", g, "Exibir o histórico preserva a pilha (duas inversões)",
                "Cadastrar 3; exibir o histórico duas vezes; consultar a última operação",
                "As duas exibições são iguais, a pilha mantém 3 operações e o topo continua #103",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    String a = codigos(c.historicoRecente());
                    String b = codigos(c.historicoRecente());
                    int topo = c.ultimaOperacao().getSolicitacao().getCodigo();
                    return r(a.equals("103,102,101") && a.equals(b) && c.quantidadeOperacoes() == 3 && topo == 103,
                            "Exibições: " + a + " e " + b + "; operações=" + c.quantidadeOperacoes() + "; topo=#" + topo);
                });

        caso("S05", g, "Histórico recente com a pilha vazia",
                "Sistema recém-iniciado: opção 7",
                "SemRegistroException",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String e = capturar(() -> c.historicoRecente());
                    return r(e.startsWith("SemRegistroException"), e);
                });

        caso("S06", g, "Pilha sem limite fixo (2000 operações)",
                "Cadastrar 1000 e atender 1000 solicitações",
                "2000 operações empilhadas; topo = ATENDIMENTO da #1100",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    for (int i = 0; i < 1000; i++) {
                        cad(c, "Cliente" + i, 1);
                    }
                    for (int i = 0; i < 1000; i++) {
                        c.atenderProxima("Técnico");
                    }
                    Operacao op = c.ultimaOperacao();
                    return r(c.quantidadeOperacoes() == 2000 && Operacao.ATENDIMENTO.equals(op.getTipo())
                            && op.getSolicitacao().getCodigo() == 1100,
                            "operações=" + c.quantidadeOperacoes() + "; topo=" + op.getTipo()
                            + " #" + op.getSolicitacao().getCodigo());
                });
    }

    // ---------------------------------------------------------------- Lista simples

    private static void listaSimples() {
        String g = "Lista simples";

        caso("L01", g, "Lista vazia: exibir, pesquisar e filtrar",
                "Sistema recém-iniciado: opções 9, 10, 12 e 13",
                "SemRegistroException (exibir); SolicitacaoNaoEncontradaException (pesquisar); filtros informam que não há resultados",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String a = capturar(() -> c.exibirTodas());
                    String b = capturar(() -> c.pesquisarPorCodigo(101));
                    String s = c.exibirPorStatus("AGUARDANDO");
                    String cat = c.exibirPorCategoria("REDE");
                    boolean ok = a.startsWith("SemRegistroException")
                            && b.startsWith("SolicitacaoNaoEncontradaException")
                            && contem(s, "nenhuma solicitação") && contem(cat, "nenhuma solicitação");
                    return r(ok, a.split(":")[0] + " | " + b.split(":")[0] + " | filtros sem resultado");
                });

        caso("L02", g, "Lista com um único elemento: pesquisar e remover",
                "Cadastrar #101, pesquisar e remover",
                "Pesquisa encontra o Ana; após remover o cadastro e a fila ficam vazios",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    String nome = c.pesquisarPorCodigo(101).getSolicitante();
                    c.remover(101);
                    String e = capturar(() -> c.exibirTodas());
                    return r(nome.equals("Ana") && c.quantidadeNoCadastro() == 0
                            && c.quantidadeAguardando() == 0 && e.startsWith("SemRegistroException"),
                            "encontrou " + nome + "; cadastro=" + c.quantidadeNoCadastro()
                            + "; fila=" + c.quantidadeAguardando());
                });

        caso("L03", g, "Remoção do primeiro elemento",
                "Cadastrar 101 a 105 e remover #101",
                "Cadastro: 102,103,104,105",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    for (int i = 0; i < 5; i++) {
                        cad(c, "Cliente" + i, 1);
                    }
                    c.remover(101);
                    String ordem = codigos(c.exibirTodas());
                    return r(ordem.equals("102,103,104,105"), "Cadastro " + ordem);
                });

        caso("L04", g, "Remoção de elemento intermediário",
                "Cadastrar 101 a 105 e remover #103",
                "Cadastro: 101,102,104,105",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    for (int i = 0; i < 5; i++) {
                        cad(c, "Cliente" + i, 1);
                    }
                    c.remover(103);
                    String ordem = codigos(c.exibirTodas());
                    return r(ordem.equals("101,102,104,105"), "Cadastro " + ordem);
                });

        caso("L05", g, "Remoção do último elemento",
                "Cadastrar 101 a 105 e remover #105",
                "Cadastro: 101,102,103,104; um novo cadastro entra normalmente no fim (#106)",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    for (int i = 0; i < 5; i++) {
                        cad(c, "Cliente" + i, 1);
                    }
                    c.remover(105);
                    String antes = codigos(c.exibirTodas());
                    cad(c, "Nova", 1);
                    String depois = codigos(c.exibirTodas());
                    return r(antes.equals("101,102,103,104") && depois.equals("101,102,103,104,106"),
                            "Antes " + antes + "; depois " + depois);
                });

        caso("L06", g, "Elemento inexistente na pesquisa",
                "Cadastrar 3 e pesquisar #999",
                "SolicitacaoNaoEncontradaException",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    String e = capturar(() -> c.pesquisarPorCodigo(999));
                    return r(e.startsWith("SolicitacaoNaoEncontradaException"), e);
                });

        caso("L07", g, "Pesquisa por código devolve os dados corretos",
                "Cadastrar Ana (REDE, prioridade 1) e Bruno (HARDWARE, prioridade 2); pesquisar #102",
                "Bruno, HARDWARE, prioridade 2",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    c.cadastrar("Bruno", "Teclado quebrado", "hardware", 2);
                    Solicitacao s = c.pesquisarPorCodigo(102);
                    return r(s.getSolicitante().equals("Bruno") && s.getCategoria().equals("HARDWARE")
                            && s.getPrioridade() == 2,
                            s.getSolicitante() + ", " + s.getCategoria() + ", prioridade " + s.getPrioridade());
                });

        caso("L08", g, "Busca por status",
                "Cadastrar 101, 102, 103; atender 101; cancelar 103; filtrar por cada status",
                "CONCLUIDA: 101; AGUARDANDO: 102; CANCELADA: 103; EM_ATENDIMENTO: nenhuma",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    c.atenderProxima("Técnico");
                    c.cancelar(103);
                    String a = codigos(c.exibirPorStatus("concluida"));
                    String b = codigos(c.exibirPorStatus("AGUARDANDO"));
                    String d = codigos(c.exibirPorStatus("CANCELADA"));
                    String e = c.exibirPorStatus("EM_ATENDIMENTO");
                    return r(a.equals("101") && b.equals("102") && d.equals("103") && contem(e, "nenhuma"),
                            "CONCLUIDA=" + a + "; AGUARDANDO=" + b + "; CANCELADA=" + d + "; EM_ATENDIMENTO sem resultado");
                });

        caso("L09", g, "Busca por categoria sem diferenciar maiúsculas de minúsculas",
                "Cadastrar categorias 'rede', 'REDE' e 'Hardware'; buscar 'Rede' e 'software'",
                "Rede: 101,102; software: nenhuma",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    c.cadastrar("Ana", "Problema", "rede", 1);
                    c.cadastrar("Bruno", "Problema", "REDE", 2);
                    c.cadastrar("Carla", "Problema", "Hardware", 1);
                    String rede = codigos(c.exibirPorCategoria("Rede"));
                    String soft = c.exibirPorCategoria("software");
                    return r(rede.equals("101,102") && contem(soft, "nenhuma"),
                            "Rede=" + rede + "; software sem resultado");
                });

        caso("L10", g, "Status inválido e categoria vazia nos filtros",
                "Filtrar por status 'ABC' e por categoria em branco",
                "CampoInvalidoException nas duas",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String a = capturar(() -> c.exibirPorStatus("ABC"));
                    String b = capturar(() -> c.exibirPorCategoria("   "));
                    return r(a.startsWith("CampoInvalidoException") && b.startsWith("CampoInvalidoException"),
                            a.split(":")[0] + " | " + b);
                });

        caso("L11", g, "Cadastro geral mantém a ordem de inserção (prioridades misturadas)",
                "Cadastrar P2, P1, P2, P1 e exibir o cadastro geral",
                "Cadastro: 101,102,103,104 (a ordem das filas não interfere)",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "A", 2); cad(c, "B", 1); cad(c, "C", 2); cad(c, "D", 1);
                    String ordem = codigos(c.exibirTodas());
                    return r(ordem.equals("101,102,103,104"), "Cadastro " + ordem);
                });
    }

    // ---------------------------------------------------------------- Lista dupla

    private static void listaDupla() {
        String g = "Lista dupla";

        caso("D01", g, "Histórico de atendimentos vazio",
                "Sistema recém-iniciado: opções 14, 15, 16 e 17",
                "SemRegistroException em todas",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String a = capturar(() -> c.historicoDoPrimeiroAoUltimo());
                    String b = capturar(() -> c.historicoDoUltimoAoPrimeiro());
                    String d = capturar(() -> c.iniciarNavegacao());
                    String e = capturar(() -> c.pesquisarAtendimento(101));
                    return r(a.startsWith("SemRegistroException") && b.startsWith("SemRegistroException")
                            && d.startsWith("SemRegistroException") && e.startsWith("SemRegistroException"),
                            "quatro operações lançaram SemRegistroException");
                });

        caso("D02", g, "Histórico com um único atendimento",
                "Atender #101 e percorrer nos dois sentidos",
                "Primeiro = último = #101; percurso direto e inverso iguais",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    c.atenderProxima("Técnico");
                    int p = c.primeiroAtendimento().getSolicitacao().getCodigo();
                    int u = c.ultimoAtendimento().getSolicitacao().getCodigo();
                    String d = codigos(c.historicoDoPrimeiroAoUltimo());
                    String i = codigos(c.historicoDoUltimoAoPrimeiro());
                    return r(p == 101 && u == 101 && d.equals("101") && i.equals("101"),
                            "primeiro=#" + p + ", último=#" + u + ", direto=" + d + ", inverso=" + i);
                });

        caso("D03", g, "Percurso direto e inverso (bidirecional)",
                "Atender #101, #102 e #103 e percorrer o histórico",
                "Direto: 101,102,103; inverso: 103,102,101",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    atenderTodas(c);
                    String d = codigos(c.historicoDoPrimeiroAoUltimo());
                    String i = codigos(c.historicoDoUltimoAoPrimeiro());
                    return r(d.equals("101,102,103") && i.equals("103,102,101"),
                            "direto=" + d + "; inverso=" + i);
                });

        caso("D04", g, "Histórico em ordem cronológica de conclusão (prioridades misturadas)",
                "Cadastrar P2 (101), P1 (102), P1 (103) e atender todas",
                "Histórico direto na ordem em que foram concluídas: 102,103,101",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 2); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    String ordem = atenderTodas(c);
                    String hist = codigos(c.historicoDoPrimeiroAoUltimo());
                    Atendimento primeiro = c.primeiroAtendimento();
                    Atendimento ultimo = c.ultimoAtendimento();
                    boolean ok = ordem.equals("102,103,101") && hist.equals("102,103,101")
                            && primeiro.getSequencia() < ultimo.getSequencia();
                    return r(ok, "atendimento=" + ordem + "; histórico=" + hist
                            + "; sequências " + primeiro.getSequencia() + " < " + ultimo.getSequencia());
                });

        caso("D05", g, "Consultar primeiro e último atendimento",
                "Atender #101, #102 e #103",
                "Primeiro = #101 e último = #103",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    atenderTodas(c);
                    int p = c.primeiroAtendimento().getSolicitacao().getCodigo();
                    int u = c.ultimoAtendimento().getSolicitacao().getCodigo();
                    return r(p == 101 && u == 103, "primeiro=#" + p + ", último=#" + u);
                });

        caso("D06", g, "Navegação anterior e próximo",
                "Atender 3; iniciar navegação; voltar duas vezes e avançar uma",
                "Começa em #103, vai a #102, depois #101 e avança para #102",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    atenderTodas(c);
                    int a = c.iniciarNavegacao().getSolicitacao().getCodigo();
                    int b = c.irParaAnterior().getSolicitacao().getCodigo();
                    int d = c.irParaAnterior().getSolicitacao().getCodigo();
                    int e = c.irParaProximo().getSolicitacao().getCodigo();
                    return r(a == 103 && b == 102 && d == 101 && e == 102,
                            "#" + a + " -> #" + b + " -> #" + d + " -> #" + e);
                });

        caso("D07", g, "Limites da navegação (início e fim do histórico)",
                "Navegar até o primeiro e tentar voltar; ir ao último e tentar avançar",
                "OperacaoInvalidaException nas duas tentativas",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1);
                    atenderTodas(c);
                    c.iniciarNavegacao();
                    String avancar = capturar(() -> c.irParaProximo());
                    c.irParaPrimeiro();
                    String voltar = capturar(() -> c.irParaAnterior());
                    return r(avancar.startsWith("OperacaoInvalidaException")
                            && voltar.startsWith("OperacaoInvalidaException"),
                            avancar + " | " + voltar);
                });

        caso("D08", g, "Ir diretamente ao primeiro e ao último",
                "Atender 3; iniciar navegação, ir ao primeiro e depois ao último",
                "#101 e depois #103",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    atenderTodas(c);
                    c.iniciarNavegacao();
                    int p = c.irParaPrimeiro().getSolicitacao().getCodigo();
                    int u = c.irParaUltimo().getSolicitacao().getCodigo();
                    return r(p == 101 && u == 103, "#" + p + " e #" + u);
                });

        caso("D09", g, "Navegar sem ter iniciado a navegação",
                "Chamar anterior/próximo sem iniciar",
                "OperacaoInvalidaException",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    c.atenderProxima("Técnico");
                    String e = capturar(() -> c.irParaAnterior());
                    return r(e.startsWith("OperacaoInvalidaException"), e);
                });

        caso("D10", g, "Pesquisar atendimento existente e inexistente",
                "Atender #101 e #102; pesquisar #102 e #999",
                "#102 encontrado (Atendimento nº 2); #999 lança SemRegistroException",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1);
                    atenderTodas(c);
                    Atendimento a = c.pesquisarAtendimento(102);
                    String e = capturar(() -> c.pesquisarAtendimento(999));
                    return r(a.getSequencia() == 2 && e.startsWith("SemRegistroException"),
                            "#102 = atendimento nº " + a.getSequencia() + " | " + e);
                });

        caso("D11", g, "Desfazer durante a navegação invalida o cursor",
                "Atender 3; iniciar navegação no #103; desfazer; consultar o atendimento atual",
                "OperacaoInvalidaException (o registro navegado foi removido)",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 1);
                    atenderTodas(c);
                    c.iniciarNavegacao();
                    c.desfazerUltimaOperacao();
                    String e = capturar(() -> c.atualNaNavegacao());
                    return r(e.startsWith("OperacaoInvalidaException") && c.quantidadeAtendimentos() == 2, e);
                });
    }

    // ---------------------------------------------------------------- Validação

    private static void validacao() {
        String g = "Validação de entradas";

        caso("V01", g, "Campos de texto vazios no cadastro (pendência apontada no 1º bimestre)",
                "Cadastrar com nome vazio, descrição vazia e categoria vazia",
                "CampoInvalidoException nas três tentativas; nada é cadastrado",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String a = capturar(() -> c.cadastrar("", "Problema", "REDE", 1));
                    String b = capturar(() -> c.cadastrar("Ana", "", "REDE", 1));
                    String d = capturar(() -> c.cadastrar("Ana", "Problema", "", 1));
                    boolean ok = a.startsWith("CampoInvalidoException") && b.startsWith("CampoInvalidoException")
                            && d.startsWith("CampoInvalidoException")
                            && c.quantidadeNoCadastro() == 0 && c.quantidadeOperacoes() == 0;
                    return r(ok, a + " | " + b + " | " + d);
                });

        caso("V02", g, "Campos só com espaços ou nulos",
                "Cadastrar com nome '   ' e com categoria nula",
                "CampoInvalidoException nas duas tentativas",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    String a = capturar(() -> c.cadastrar("   ", "Problema", "REDE", 1));
                    String b = capturar(() -> c.cadastrar("Ana", "Problema", null, 1));
                    return r(a.startsWith("CampoInvalidoException") && b.startsWith("CampoInvalidoException"),
                            a + " | " + b);
                });

        caso("V03", g, "Responsável vazio no atendimento",
                "Cadastrar #101 e atender com responsável vazio",
                "CampoInvalidoException; a solicitação continua na fila",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    String e = capturar(() -> c.atenderProxima(" "));
                    return r(e.startsWith("CampoInvalidoException") && c.quantidadeAguardando() == 1
                            && c.quantidadeAtendimentos() == 0, e + " | aguardando=" + c.quantidadeAguardando());
                });

        caso("V04", g, "Códigos sequenciais e únicos",
                "Cadastrar 3 solicitações",
                "Códigos 101, 102 e 103",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    int a = cad(c, "Ana", 1).getCodigo();
                    int b = cad(c, "Bruno", 2).getCodigo();
                    int d = cad(c, "Carla", 1).getCodigo();
                    return r(a == 101 && b == 102 && d == 103, a + ", " + b + ", " + d);
                });

        caso("V05", g, "Textos são normalizados (espaços removidos, categoria em maiúsculas)",
                "Cadastrar '  Ana  ' com categoria '  rede '",
                "Solicitante 'Ana' e categoria 'REDE'",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    Solicitacao s = c.cadastrar("  Ana  ", "  Sem acesso  ", "  rede ", 1);
                    return r(s.getSolicitante().equals("Ana") && s.getCategoria().equals("REDE"),
                            "'" + s.getSolicitante() + "' e '" + s.getCategoria() + "'");
                });
    }

    // ---------------------------------------------------------------- Integração

    private static void integracao() {
        String g = "Integração";

        caso("I01", g, "Fluxo completo: cadastro geral -> fila -> atendimento -> pilha e lista dupla",
                "Cadastrar #101 e atendê-la",
                "Cadastro 1 (CONCLUIDA), fila 0, atendimentos 1, operações 2",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1);
                    c.atenderProxima("Técnico");
                    boolean ok = c.quantidadeNoCadastro() == 1
                            && c.quantidadePorStatus(Solicitacao.CONCLUIDA) == 1
                            && c.quantidadeAguardando() == 0 && c.quantidadeAtendimentos() == 1
                            && c.quantidadeOperacoes() == 2;
                    return r(ok, "cadastro=" + c.quantidadeNoCadastro() + ", concluídas="
                            + c.quantidadePorStatus(Solicitacao.CONCLUIDA) + ", fila=" + c.quantidadeAguardando()
                            + ", atendimentos=" + c.quantidadeAtendimentos() + ", operações=" + c.quantidadeOperacoes());
                });

        caso("I02", g, "Consistência após sequência mista de operações",
                "Cadastrar 6 (P1,P2,P1,P2,P1,P2); cancelar #102; remover #103; atender 2; desfazer 1",
                "Cadastro 5; AGUARDANDO 3; CONCLUIDA 1; CANCELADA 1; fila 3; atendimentos 1",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    for (int i = 0; i < 6; i++) {
                        cad(c, "Cliente" + i, (i % 2 == 0) ? 1 : 2);
                    }
                    c.cancelar(102);
                    c.remover(103);
                    c.atenderProxima("Técnico");
                    c.atenderProxima("Técnico");
                    c.desfazerUltimaOperacao();
                    int cad = c.quantidadeNoCadastro();
                    int ag = c.quantidadePorStatus(Solicitacao.AGUARDANDO);
                    int co = c.quantidadePorStatus(Solicitacao.CONCLUIDA);
                    int ca = c.quantidadePorStatus(Solicitacao.CANCELADA);
                    boolean ok = cad == 5 && ag == 3 && co == 1 && ca == 1
                            && c.quantidadeAguardando() == ag && c.quantidadeAtendimentos() == co
                            && cad == ag + co + ca;
                    return r(ok, "cadastro=" + cad + ", AGUARDANDO=" + ag + ", CONCLUIDA=" + co
                            + ", CANCELADA=" + ca + ", fila=" + c.quantidadeAguardando()
                            + ", atendimentos=" + c.quantidadeAtendimentos());
                });

        caso("I03", g, "Resumo do sistema reflete as quatro estruturas",
                "Cadastrar 3 (P1,P1,P2), atender 1 e cancelar 1; exibir o resumo",
                "Resumo informa 3 no cadastro, 1 concluída, 1 cancelada, 1 atendimento e 5 operações",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "Ana", 1); cad(c, "Bruno", 1); cad(c, "Carla", 2);
                    c.atenderProxima("Técnico");
                    c.cancelar(103);
                    String t = c.resumo();
                    boolean ok = contem(t, "cadastro geral ..: 3") && contem(t, "CONCLUIDA .....................: 1")
                            && contem(t, "CANCELADA .....................: 1")
                            && contem(t, "(histórico): 1") && contem(t, "recente ..: 5");
                    return r(ok, "campos do resumo conferidos");
                });

        caso("I04", g, "Cenário completo de demonstração (cadastrar, cancelar, atender, desfazer)",
                "Cadastrar 5 (P1,P1,P2,P1,P2); cancelar #102; atender até esvaziar; desfazer o último atendimento",
                "Ordem de atendimento 101,104,103,105; após desfazer a #105 volta à fila",
                () -> {
                    CentralAtendimento c = new CentralAtendimento();
                    cad(c, "A", 1); cad(c, "B", 1); cad(c, "C", 2); cad(c, "D", 1); cad(c, "E", 2);
                    c.cancelar(102);
                    String ordem = atenderTodas(c);
                    c.desfazerUltimaOperacao();
                    int prox = c.consultarProxima().getCodigo();
                    return r(ordem.equals("101,104,103,105") && prox == 105,
                            "Ordem " + ordem + "; após desfazer, próxima = #" + prox);
                });
    }
}
