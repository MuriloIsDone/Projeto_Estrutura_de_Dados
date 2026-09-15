package ui;

import java.util.Scanner;

import model.Operacao;
import model.Solicitacao;
import service.CentralAtendimento;

/**
 * Camada de interface com o usuario. Nao contem regra de negocio:
 * apenas le dados, chama a CentralAtendimento e exibe resultados.
 */
public class Menu {

    private Scanner entrada;
    private CentralAtendimento central;

    public Menu() {
        this.entrada = new Scanner(System.in);
        this.central = new CentralAtendimento();
    }

    public void executar() {
        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opcao: ", 0, 9);
            System.out.println();
            try {
                switch (opcao) {
                    case 1: cadastrarSolicitacao();  break;
                    case 2: consultarProxima();      break;
                    case 3: atenderProxima();        break;
                    case 4: exibirFila();            break;
                    case 5: exibirQuantidade();      break;
                    case 6: consultarUltimaOperacao(); break;
                    case 7: exibirHistorico();       break;
                    case 8: desfazerUltimaOperacao(); break;
                    case 9: exibirResumo();          break;
                    case 0: System.out.println("Encerrando o sistema. Ate mais!"); break;
                }
            } catch (Exception e) {
                // Todas as excecoes lancadas pelas estruturas e pela regra de
                // negocio sao tratadas aqui, em um unico ponto.
                System.out.println("  >> Aviso: " + e.getMessage());
            }
            if (opcao != 0) pausar();
        } while (opcao != 0);
        entrada.close();
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("         CENTRAL DE ATENDIMENTO         ");
        System.out.println("========================================");
        System.out.println("1 - Cadastrar nova solicitacao");
        System.out.println("2 - Consultar proxima solicitacao");
        System.out.println("3 - Atender proxima solicitacao");
        System.out.println("4 - Exibir fila de solicitacoes");
        System.out.println("5 - Exibir quantidade de solicitacoes");
        System.out.println("6 - Consultar ultima operacao realizada");
        System.out.println("7 - Exibir historico de operacoes");
        System.out.println("8 - Desfazer ultima operacao");
        System.out.println("9 - Exibir resumo do sistema");
        System.out.println("0 - Encerrar");
        System.out.println("----------------------------------------");
    }

    // ---------------------------------------------------------------- opcoes

    private void cadastrarSolicitacao() throws Exception {
        System.out.println("--- Cadastro de solicitacao ---");
        String solicitante = lerTexto("Nome do solicitante: ");
        String descricao   = lerTexto("Descricao do problema: ");
        String categoria   = lerTexto("Categoria (ex.: REDE, HARDWARE, SOFTWARE): ");
        int prioridade     = lerInteiro("Prioridade (1 = alta ... 5 = baixa): ", 1, 5);

        Solicitacao s = central.cadastrar(solicitante, descricao, categoria, prioridade);
        System.out.println("  Solicitacao cadastrada e inserida no fim da fila:");
        System.out.println("  " + s);
    }

    private void consultarProxima() throws Exception {
        System.out.println("  Proxima da fila (sem remover):");
        System.out.println("  " + central.consultarProxima());
    }

    private void atenderProxima() throws Exception {
        Solicitacao proxima = central.consultarProxima();   // valida antes de pedir dados
        System.out.println("  Sera atendida: " + proxima);
        String responsavel = lerTexto("Responsavel pelo atendimento: ");
        Solicitacao s = central.atenderProxima(responsavel);
        System.out.println("  Atendimento concluido:");
        System.out.println("  " + s);
    }

    private void exibirFila() throws Exception {
        System.out.println("--- Fila de solicitacoes (do inicio para o fim) ---");
        System.out.print(central.filaFormatada());
    }

    private void exibirQuantidade() {
        System.out.println("  Solicitacoes aguardando atendimento: "
                + central.quantidadeAguardando());
        System.out.println("  Fila vazia? " + (central.filaVazia() ? "sim" : "nao"));
    }

    private void consultarUltimaOperacao() throws Exception {
        Operacao op = central.ultimaOperacao();
        System.out.println("  Ultima operacao (topo da pilha): " + op);
    }

    private void exibirHistorico() throws Exception {
        System.out.println("--- Historico de operacoes (da mais recente para a mais antiga) ---");
        System.out.print(central.historicoFormatado());
    }

    private void desfazerUltimaOperacao() throws Exception {
        Solicitacao s = central.desfazerUltimaOperacao();
        System.out.println("  Atendimento desfeito. A solicitacao voltou para o inicio da fila:");
        System.out.println("  " + s);
    }

    private void exibirResumo() {
        System.out.println("--- Resumo do sistema ---");
        System.out.println(central.resumo());
    }

    // ------------------------------------------------------- leitura validada

    private String lerTexto(String rotulo) {
        String texto;
        do {
            System.out.print(rotulo);
            texto = entrada.nextLine().trim();
            if (texto.isEmpty())
                System.out.println("  >> O campo nao pode ficar vazio.");
        } while (texto.isEmpty());
        return texto;
    }

    private int lerInteiro(String rotulo, int min, int max) {
        while (true) {
            System.out.print(rotulo);
            String linha = entrada.nextLine().trim();
            try {
                int valor = Integer.parseInt(linha);
                if (valor < min || valor > max)
                    System.out.println("  >> Informe um valor entre " + min + " e " + max + ".");
                else
                    return valor;
            } catch (NumberFormatException e) {
                System.out.println("  >> Valor invalido: digite um numero inteiro.");
            }
        }
    }

    private void pausar() {
        System.out.print("\n[ENTER para continuar]");
        entrada.nextLine();
    }
}
