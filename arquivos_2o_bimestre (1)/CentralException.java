package excecoes;

//Exceção base das situações de erro tratáveis da Central de Atendimento.
// As exceções específicas do sistema ficam declaradas aqui, como classes
// internas, para que todas permaneçam reunidas em um único arquivo. O menu
// trata qualquer erro do sistema capturando apenas CentralException.

public class CentralException extends Exception {

    private static final long serialVersionUID = 1L;

    public CentralException(String mensagem) {
        super(mensagem);
    }

    /** Campo de texto obrigatório vazio ou valor informado não aceito. */
    public static class CampoInvalidoException extends CentralException {
        private static final long serialVersionUID = 1L;

        public CampoInvalidoException(String mensagem) {
            super(mensagem);
        }
    }

    /** Prioridade diferente de 1 (alta) e 2 (baixa). */
    public static class PrioridadeInvalidaException extends CentralException {
        private static final long serialVersionUID = 1L;

        public PrioridadeInvalidaException(String mensagem) {
            super(mensagem);
        }
    }

    /** Nenhuma solicitação aguardando atendimento nas filas. */
    public static class SemSolicitacaoException extends CentralException {
        private static final long serialVersionUID = 1L;

        public SemSolicitacaoException(String mensagem) {
            super(mensagem);
        }
    }

    // Histórico de operações ou histórico de atendimentos vazio.
    public static class SemRegistroException extends CentralException {
        private static final long serialVersionUID = 1L;

        public SemRegistroException(String mensagem) {
            super(mensagem);
        }
    }

    // Código informado não existe no cadastro geral.
    public static class SolicitacaoNaoEncontradaException extends CentralException {
        private static final long serialVersionUID = 1L;

        public SolicitacaoNaoEncontradaException(String mensagem) {
            super(mensagem);
        }
    }

    // Operação não permitida pelas regras do sistema.
    public static class OperacaoInvalidaException extends CentralException {
        private static final long serialVersionUID = 1L;

        public OperacaoInvalidaException(String mensagem) {
            super(mensagem);
        }
    }

    //Falha interna inesperada ao usar uma estrutura de dados. As classes do
    // professor declaram "throws Exception"; como o sistema verifica as
    // condições antes de cada chamada, esta exceção não deve ocorrer em uso
    // normal. É não verificada por indicar defeito de programação.

    public static class EstruturaInconsistenteException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public EstruturaInconsistenteException(String mensagem, Throwable causa) {
            super(mensagem, causa);
        }

        public EstruturaInconsistenteException(String mensagem) {
            super(mensagem);
        }
    }
}
