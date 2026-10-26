package estruturas;

// Classe Pilha<T>: relativa a pilha dinâmica e encadeada.
// Implementada como uma lista encadeada com nós do tipo classe Node<T>
// Pilha Genérica, de qualquer tipo de dado não primitivo.
// Autor1: Ivan Carlos Alcântara de Oliveira.
// Data da Criação: 10/10/2026. 15h.
public class Pilha<T> {
    private Node<T> topoPilha;
    private int quantidade;

    // Construtor 
    public Pilha(int tamanho) {
        this.topoPilha = null;
        this.quantidade = 0;
    }

    // Verifica se a pilha esta vazia.
    public boolean isEmpty() {
        return this.topoPilha == null;
    }

    // Verifica se a lista está cheia.
    // Como a lista utiliza alocação dinâmica de memória,
    // não possui capacidade máxima predefinida.
    // Caso não haja memória suficiente, 
    // ocorre o erro em Java (OutOfMemoryError).
    // @return false, pois a lista não possui limite fixo.
    public boolean isFull() {
       return false;
    }

    // Insere um elemento no topo da pilha.
    public void push(T e) throws Exception {
        if (!this.isFull()) {
            this.topoPilha = new Node<T>(e, this.topoPilha);
            this.quantidade++;
        } else {
            throw new Exception("overflow - Estouro de Pilha");
        }
    }

    // Remove e retorna o elemento do topo da pilha.
    public T pop() throws Exception {
        if (!this.isEmpty()) {
            T elemento = this.topoPilha.getDado();
            this.topoPilha = this.topoPilha.getProx();
            this.quantidade--;
            return elemento;
        } else {
            throw new Exception("underflow - Esvaziamento de Pilha");
        }
    }

    // Retorna o elemento do topo sem remove-lo.
    public T topo() throws Exception {
        if (!this.isEmpty()) {
            return this.topoPilha.getDado();
        } else {
            throw new Exception("Underlow - Esvaziamento de Pilha");
        }
    }

    // Retorna a quantidade de elementos armazenados.
    public int sizeElements() {
        return this.quantidade;
    }

    // Representacao textual, do fundo para o topo.
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[Pilha] topoPilha: ")
          .append(quantidade - 1)
          .append(", quantidade de elementos: ")
          .append(sizeElements());

        if (!isEmpty()) {
            sb.append(", valor do Topo: ").append(topoPilha.getDado());
        } else {
            sb.append(", valor do Topo: PILHA VAZIA");
        }

        sb.append("\nConteudo da Pilha': [ ");
        Object[] elementos = new Object[quantidade];
        Node<T> atual = topoPilha;
        for (int i = quantidade - 1; i >= 0; i--) {
            elementos[i] = atual.getDado();
            atual = atual.getProx();
        }
        for (int i = 0; i < quantidade; i++) {
            if (i > 0) sb.append(", ");
            sb.append(elementos[i]);
        }
        sb.append(" ]");
        return sb.toString();
    }
}
