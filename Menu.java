package ui;

import java.util.Scanner;

import model.Operacao;
import model.Solicitacao;
import service.CentralAtendimento;

// camada de interface, so le dado e chama a central, regra de negocio fica la
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
                // pego tudo aqui pra n ficar repetindo try/catch em cada opcao
                System.out.println("  >> Aviso: " + e.getMessage());
            }
            if (opcao != 0) pausar();
        } while (opcao != 0);
        entrada.close();
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("========================================");
        System.out.println("         CENTRAL DE ATENDÍMENTO         ");
        System.out.println("========================================");
        System.out.println("1 - cadastrar nova solicitacao");
        System.out.println("2 - consultar proxima solicitacao");
        System.out.println("3 - atender proxima solicitacao");
        System.out.println("4 - exibir fila de solicitacoes");
        System.out.println("5 - exibir quantidade de solicitacoes");
        System.out.println("6 - eonsultar ultima operacao realizada");
        System.out.println("7 - exibir historico de operacoes");
        System.out.println("8 - desfazer ultima operacao");
        System.out.println("9 - exibir resumo do sistema");
        System.out.println("0 - encerrar");
        System.out.println("----------------------------------------");
    }

    //opcoes

    private void cadastrarSolicitacao() throws Exception {
        System.out.println("- Cadastro de solicitacao -");
        String solicitante = lerTexto("Nome do solicitante:");
        String descricao   = lerTexto("descricao do problema: ");
        String categoria   = lerTexto("categoria (tipo: Rede, HARDWARE, SOFTWARE):");
        int prioridade     = lerInteiro("prioridade (1 = alta ... 5 = baixa):", 1, 5);

        Solicitacao s = central.cadastrar(solicitante, descricao, categoria, prioridade);
        System.out.println("solicitacao cadastrada e inserida no fim da fila:");
        System.out.println("  " + s);
    }

    private void consultarProxima() throws Exception {
        System.out.println("proxima da fila (sem remover):");
        System.out.println("  " + central.consultarProxima());
    }

    private void atenderProxima() throws Exception {
        Solicitacao proxima = central.consultarProxima(); // valida antes de pedir o nome do responsavel   // valida antes de pedir dados
        System.out.println("sera atendida: " + proxima);
        String responsavel = lerTexto("responsavel pelo atendimento: ");
        Solicitacao s = central.atenderProxima(responsavel);
        System.out.println("atendimento concluido:");
        System.out.println("  " + s);
    }

    private void exibirFila() throws Exception {
        System.out.println("...fila de solicitacoes (do inicio para o fim)...");
        System.out.print(central.filaFormatada());
    }

    private void exibirQuantidade() {
        System.out.println("solicitacoes aguardando atendimento: "
                + central.quantidadeAguardando());
        System.out.println("fila vazia? " + (central.filaVazia() ? "sim" : "nao"));
    }

    private void consultarUltimaOperacao() throws Exception {
        Operacao op = central.ultimaOperacao();
        System.out.println("ultima operacao (topo da pilha): " + op);
    }

    private void exibirHistorico() throws Exception {
        System.out.println("historico de operacoes (da mais recente para a mais antiga)");
        System.out.print(central.historicoFormatado());
    }

    private void desfazerUltimaOperacao() throws Exception {
        Solicitacao s = central.desfazerUltimaOperacao();
        System.out.println("atendimento desfeito. a solicitacao voltou para o inicio da fila:");
        System.out.println("  " + s);
    }

    private void exibirResumo() {
        System.out.println("resumo do sistema");
        System.out.println(central.resumo());
    }

    //leitura simplificada

    private String lerTexto(String rotulo) {
        System.out.print(rotulo);
        return entrada.nextLine();
    }

    private int lerInteiro(String rotulo, int min, int max) {
        while (true) {
            System.out.print(rotulo);
            String linha = entrada.nextLine(); 
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