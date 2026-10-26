package ui;

import java.util.Scanner;

import excecoes.CentralException;
import model.Atendimento;
import model.Operacao;
import model.Solicitacao;
import service.CentralAtendimento;

/**
 * Camada de interface com o usuário (menu textual).
 * Esta classe não contém regra de negócio: apenas lê dados, valida o formato
 * da entrada, chama a CentralAtendimento e exibe os resultados. Todas as
 * exceções da Central são tratadas em um único ponto, no laço principal.
 */
public class Menu {

    private final Scanner entrada = new Scanner(System.in);
    private final CentralAtendimento central = new CentralAtendimento();

    public void executar() {
        int opcao;
        do {
            exibirMenu();
            opcao = lerInteiro("Escolha uma opção: ", 0, 22);
            System.out.println();
            try {
                tratarOpcao(opcao);
            } catch (CentralException e) {
                System.out.println("  >> Aviso: " + e.getMessage());
            }
            if (opcao != 0) {
                pausar();
            }
        } while (opcao != 0);
        entrada.close();
    }

    private void tratarOpcao(int opcao) throws CentralException {
        switch (opcao) {
            case 1:  cadastrarSolicitacao(); break;
            case 2:  consultarProxima(); break;
            case 3:  atenderProxima(); break;
            case 4:  exibirFilaDeEspera(); break;
            case 5:  exibirQuantidadeAguardando(); break;
            case 6:  consultarUltimaOperacao(); break;
            case 7:  exibirHistoricoRecente(); break;
            case 8:  desfazerUltimaOperacao(); break;
            case 9:  exibirTodasAsSolicitacoes(); break;
            case 10: pesquisarPorCodigo(); break;
            case 11: removerSolicitacao(); break;
            case 12: exibirPorStatus(); break;
            case 13: exibirPorCategoria(); break;
            case 14: exibirHistoricoDoPrimeiroAoUltimo(); break;
            case 15: exibirHistoricoDoUltimoAoPrimeiro(); break;
            case 16: navegarPeloHistorico(); break;
            case 17: pesquisarAtendimento(); break;
            case 18: exibirResumo(); break;
            case 19: exibirSolicitacoesPorPrioridade(); break;
            case 20: exibirQuantidadesPorPrioridade(); break;
            case 21: demonstrarSelecao(); break;
            case 22: cancelarSolicitacao(); break;
            case 0:  System.out.println("Encerrando o sistema. Até mais!"); break;
            default: break;
        }
    }

    private void exibirMenu() {
        System.out.println();
        System.out.println("=================================================");
        System.out.println("    CENTRAL DE ATENDIMENTO E SERVIÇOS");
        System.out.println("=================================================");
        System.out.println("ATENDIMENTO");
        System.out.println(" 1 - Cadastrar nova solicitação");
        System.out.println(" 2 - Consultar próxima solicitação");
        System.out.println(" 3 - Atender próxima solicitação");
        System.out.println(" 4 - Exibir fila de espera");
        System.out.println(" 5 - Quantidade de solicitações aguardando");
        System.out.println("OPERAÇÕES");
        System.out.println(" 6 - Consultar última operação");
        System.out.println(" 7 - Exibir histórico recente");
        System.out.println(" 8 - Desfazer última operação");
        System.out.println("CADASTRO GERAL");
        System.out.println(" 9 - Exibir todas as solicitações");
        System.out.println("10 - Pesquisar solicitação por código");
        System.out.println("11 - Remover solicitação");
        System.out.println("12 - Exibir solicitações por status");
        System.out.println("13 - Exibir solicitações por categoria");
        System.out.println("HISTÓRICO DE ATENDIMENTOS");
        System.out.println("14 - Exibir do primeiro ao último");
        System.out.println("15 - Exibir do último ao primeiro");
        System.out.println("16 - Navegar pelo histórico");
        System.out.println("17 - Pesquisar atendimento");
        System.out.println("RELATÓRIOS");
        System.out.println("18 - Exibir resumo do sistema");
        System.out.println("PRIORIDADES");
        System.out.println("19 - Exibir solicitações por prioridade");
        System.out.println("20 - Consultar quantidades por prioridade");
        System.out.println("21 - Demonstrar a seleção da próxima solicitação");
        System.out.println("22 - Cancelar solicitação");
        System.out.println(" 0 - Encerrar");
        System.out.println("-------------------------------------------------");
    }

    // ------------------------------------------------------------ atendimento

    private void cadastrarSolicitacao() throws CentralException {
        System.out.println("--- Cadastro de solicitação ---");
        String solicitante = lerTexto("Nome do solicitante: ");
        String descricao = lerTexto("Descrição do problema: ");
        String categoria = lerTexto("Categoria (ex.: REDE, HARDWARE, SOFTWARE): ");
        int prioridade = lerInteiro("Prioridade (1 = alta, 2 = baixa): ", 1, 2);

        Solicitacao s = central.cadastrar(solicitante, descricao, categoria, prioridade);
        System.out.println("  Solicitação cadastrada no cadastro geral e inserida no fim da fila de prioridade "
                + s.getPrioridade() + ":");
        System.out.println("  " + s);
    }

    private void consultarProxima() throws CentralException {
        System.out.println("  Próxima solicitação (sem remover):");
        System.out.println("  " + central.consultarProxima());
    }

    private void atenderProxima() throws CentralException {
        Solicitacao proxima = central.consultarProxima();   // valida antes de pedir dados
        System.out.println("  Será atendida: " + proxima);
        String responsavel = lerTexto("Responsável pelo atendimento: ");
        Solicitacao s = central.atenderProxima(responsavel);
        System.out.println("  Atendimento concluído:");
        System.out.println("  " + s);
    }

    private void exibirFilaDeEspera() {
        System.out.print(central.exibirFilas());
    }

    private void exibirQuantidadeAguardando() {
        System.out.println("  Solicitações aguardando atendimento: " + central.quantidadeAguardando());
    }

    // -------------------------------------------------------------- operações

    private void consultarUltimaOperacao() throws CentralException {
        Operacao op = central.ultimaOperacao();
        System.out.println("  Última operação (topo da pilha): " + op);
    }

    private void exibirHistoricoRecente() throws CentralException {
        System.out.println("--- Histórico recente (da mais recente para a mais antiga) ---");
        System.out.print(central.historicoRecente());
    }

    private void desfazerUltimaOperacao() throws CentralException {
        Solicitacao s = central.desfazerUltimaOperacao();
        System.out.println("  Atendimento desfeito. A solicitação voltou para o início da fila de prioridade "
                + s.getPrioridade() + ":");
        System.out.println("  " + s);
    }

    // --------------------------------------------------------- cadastro geral

    private void exibirTodasAsSolicitacoes() throws CentralException {
        System.out.println("--- Cadastro geral de solicitações ---");
        System.out.print(central.exibirTodas());
    }

    private void pesquisarPorCodigo() throws CentralException {
        int codigo = lerInteiro("Código da solicitação: ", 1, Integer.MAX_VALUE);
        System.out.println("  " + central.pesquisarPorCodigo(codigo));
    }

    private void removerSolicitacao() throws CentralException {
        int codigo = lerInteiro("Código da solicitação a remover: ", 1, Integer.MAX_VALUE);
        Solicitacao s = central.remover(codigo);
        System.out.println("  Solicitação removida do cadastro geral:");
        System.out.println("  " + s);
    }

    private void exibirPorStatus() throws CentralException {
        String status = lerTexto("Status (AGUARDANDO, EM_ATENDIMENTO, CONCLUIDA ou CANCELADA): ");
        System.out.print(central.exibirPorStatus(status));
    }

    private void exibirPorCategoria() throws CentralException {
        String categoria = lerTexto("Categoria: ");
        System.out.print(central.exibirPorCategoria(categoria));
    }

    private void cancelarSolicitacao() throws CentralException {
        int codigo = lerInteiro("Código da solicitação a cancelar: ", 1, Integer.MAX_VALUE);
        Solicitacao s = central.cancelar(codigo);
        System.out.println("  Solicitação cancelada e retirada da fila de espera:");
        System.out.println("  " + s);
    }

    // ------------------------------------------------- histórico de atendimentos

    private void exibirHistoricoDoPrimeiroAoUltimo() throws CentralException {
        System.out.println("--- Atendimentos concluídos (do primeiro ao último) ---");
        System.out.print(central.historicoDoPrimeiroAoUltimo());
    }

    private void exibirHistoricoDoUltimoAoPrimeiro() throws CentralException {
        System.out.println("--- Atendimentos concluídos (do último ao primeiro) ---");
        System.out.print(central.historicoDoUltimoAoPrimeiro());
    }

    private void navegarPeloHistorico() throws CentralException {
        Atendimento atual = central.iniciarNavegacao();
        boolean navegando = true;
        while (navegando) {
            System.out.println("  Atendimento atual: " + atual);
            System.out.print("  [A] Anterior  [P] Próximo  [I] Primeiro  [F] Último  [S] Sair: ");
            String tecla = entrada.nextLine().trim().toUpperCase();
            try {
                switch (tecla) {
                    case "A": atual = central.irParaAnterior(); break;
                    case "P": atual = central.irParaProximo(); break;
                    case "I": atual = central.irParaPrimeiro(); break;
                    case "F": atual = central.irParaUltimo(); break;
                    case "S": navegando = false; break;
                    default:  System.out.println("  >> Opção inválida. Use A, P, I, F ou S."); break;
                }
            } catch (CentralException e) {
                System.out.println("  >> Aviso: " + e.getMessage());
            }
        }
        central.encerrarNavegacao();
    }

    private void pesquisarAtendimento() throws CentralException {
        int codigo = lerInteiro("Código da solicitação atendida: ", 1, Integer.MAX_VALUE);
        System.out.println("  " + central.pesquisarAtendimento(codigo));
    }

    // -------------------------------------------------------------- relatórios

    private void exibirResumo() {
        System.out.println("--- Resumo do sistema ---");
        System.out.println(central.resumo());
    }

    // -------------------------------------------------------------- prioridades

    private void exibirSolicitacoesPorPrioridade() throws CentralException {
        int prioridade = lerInteiro("Prioridade (1 = alta, 2 = baixa): ", 1, 2);
        System.out.println("--- Fila de prioridade " + prioridade + " ---");
        System.out.print(central.exibirFila(prioridade));
    }

    private void exibirQuantidadesPorPrioridade() throws CentralException {
        System.out.println("  Prioridade 1 (alta) : " + central.quantidadeAguardando(Solicitacao.PRIORIDADE_ALTA));
        System.out.println("  Prioridade 2 (baixa): " + central.quantidadeAguardando(Solicitacao.PRIORIDADE_BAIXA));
        System.out.println("  Total aguardando    : " + central.quantidadeAguardando());
    }

    private void demonstrarSelecao() {
        System.out.println("--- Seleção da próxima solicitação ---");
        System.out.print(central.demonstrarSelecao());
    }

    // ----------------------------------------------------- leitura com validação

    private String lerTexto(String rotulo) {
        String texto;
        do {
            System.out.print(rotulo);
            texto = entrada.nextLine().trim();
            if (texto.isEmpty()) {
                System.out.println("  >> O campo não pode ficar vazio.");
            }
        } while (texto.isEmpty());
        return texto;
    }

    private int lerInteiro(String rotulo, int minimo, int maximo) {
        while (true) {
            System.out.print(rotulo);
            String linha = entrada.nextLine().trim();
            try {
                int valor = Integer.parseInt(linha);
                if (valor < minimo || valor > maximo) {
                    System.out.println("  >> Informe um valor entre " + minimo + " e " + maximo + ".");
                } else {
                    return valor;
                }
            } catch (NumberFormatException e) {
                System.out.println("  >> Valor inválido: digite um número inteiro.");
            }
        }
    }

    private void pausar() {
        System.out.print("\n[ENTER para continuar]");
        entrada.nextLine();
    }
}
