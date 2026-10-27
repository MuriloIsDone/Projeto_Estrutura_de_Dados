package estruturas;

// Classe DLL<T>: relativa a lista duplamente encadeada, ordenada, dinâmica
// não circular. tiliza a classe NodeDLL<T>.
// Lista genérica de qualquer tipo de dado não primitivo.
// Autor1: Ivan Carlos Alcântara de Oliveira.
// Data da Criação: 10/10/2026. 15h.
public class DLL<T extends Comparable<T>> {
    private NodeDLL<T> head;
    private int size;

    // construtor da DLL
    public DLL() {
        head = null;
        size = 0;
    }

    // verifica se lista vazia, retornando true se verdadeiro.   
    public boolean isEmpty() {
        return head == null;
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

    // retorna a quantidade de elementos da lista
    public int getSize() {
        return size;
    }

    // Retorna o NodeDLL cabeça de lista 
    public NodeDLL<T> getHead() {
        return head;
    }

    // Mostra o conteúdo da lista em ordem crescente
    public void showAscending() {
        if (isEmpty()) {
            System.out.print("Lista vazia!\n");
            return;
        }
        System.out.print("Lista: [ ");
        NodeDLL<T> atual = head;
        while (atual != null) {
            System.out.print(atual.getData());
            atual = atual.getRight();
            if (atual != null) System.out.print(" ");
        }
        System.out.print("]");
    }

    // Mostra o conteúdo da lista em ordem decrescente
    public void showDescending() {
        if (isEmpty()) {
            System.out.print("Lista vazia!\n");
            return;
        }
        NodeDLL<T> atual = head;
        while (atual.getRight() != null) atual = atual.getRight();
        System.out.print("Lista: [ ");
        while (atual != null) {
            System.out.print(atual.getData());
            atual = atual.getLeft();
            if (atual != null) System.out.print(" ");
        }
        System.out.print("]");
    }

    // Insere um dado (data) na lista em ordem crescente
    public boolean insertAscending(T data) {
        if (isFull()) return false;
        NodeDLL<T> novo = new NodeDLL<T>(data, null, null);
        if (isEmpty()) {
            head = novo;
        } else if (head.getData().compareTo(data) > 0) {
            novo.setRight(head);
            head.setLeft(novo);
            head = novo;
        } else {
            NodeDLL<T> atual = head;
            // Insere depois de valores iguais, mantendo a ordem crescente.
            while (atual.getRight() != null &&
                   atual.getRight().getData().compareTo(data) <= 0) {
                atual = atual.getRight();
            }
            novo.setLeft(atual);
            novo.setRight(atual.getRight());
            if (atual.getRight() != null) atual.getRight().setLeft(novo);
            atual.setRight(novo);
        }
        size++;
        return true;
    }

    // Posicoes iniciam em 1; retorna null se a posicao for invalida.
    public NodeDLL<T> get(int pos) {
        if (pos <= 0 || pos > size) return null;
        NodeDLL<T> atual = head;
        for (int i = 1; i < pos; i++) atual = atual.getRight();
        return atual;
    }
  
   // Remove todos os nós da lista duplamente encadeada,
   // desfazendo suas referências aos nós anterior e próximo.
   // Ao final, define o início da lista como null
   // e atualiza seu tamanho para zero.
    public void clear() {
        NodeDLL<T> atual = head;
        while (atual != null) {
            NodeDLL<T> proximo = atual.getRight();
            atual.setLeft(null);
            atual.setRight(null);
            atual = proximo;
        }
        head = null;
        size = 0;
    }

    // Retorna a posicao (a partir de 1) ou -1 se nao encontrado.
    public int searchAscending(T data) {
        NodeDLL<T> atual = head;
        int pos = 1;
        while (atual != null && atual.getData().compareTo(data) < 0) {
            atual = atual.getRight();
            pos++;
        }
        return atual != null && atual.getData().compareTo(data) == 0 ? pos : -1;
    }

    // Remove a primeira ocorrencia do valor informado.
    public boolean remove(T data) {
        NodeDLL<T> atual = head;
        while (atual != null && atual.getData().compareTo(data) < 0)
            atual = atual.getRight();
        if (atual == null || atual.getData().compareTo(data) != 0) return false;
        unlink(atual);
        return true;
    }

    // Remove e retorna o no na posicao (a partir de 1).
    public NodeDLL<T> remove(int pos) {
        NodeDLL<T> atual = get(pos);
        if (atual == null) return null;
        unlink(atual);
        return atual;
    }

    // Remove um nó de uma lista duplamente encadeada,
    // ajustando as referências dos nós anterior e próximo.
    // Atualiza o início da lista, quando necessário,
    // desvincula o nó removido e decrementa seu tamanho.
    private void unlink(NodeDLL<T> atual) {
        NodeDLL<T> anterior = atual.getLeft();
        NodeDLL<T> proximo = atual.getRight();
        if (anterior == null) head = proximo;
        else anterior.setRight(proximo);
        if (proximo != null) proximo.setLeft(anterior);
        atual.setLeft(null);
        atual.setRight(null);
        size--;
    }

    // Retorna uma representação textual da lista duplamente encadeada,
    // percorrendo seus nós do início ao fim e exibindo seus elementos
    // na ordem em que estão armazenados.
    // Ao final, apresenta a quantidade de elementos da lista. 
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("\n[Lista]\nL: [ ");
        NodeDLL<T> atual = head;
        while (atual != null) {
            sb.append(atual.getData());
            atual = atual.getRight();
            if (atual != null) sb.append(" ");
        }
        sb.append(" ]\nQtde.: ").append(size);
        return sb.toString();
    }
}
