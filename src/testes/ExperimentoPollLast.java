package testes;

import estruturas.LinkedList;

/**
 * Experimento que reproduz um defeito encontrado no método pollLast() da
 * LinkedList fornecida pelo professor. Como as classes do professor não podem
 * ser alteradas, o sistema não utiliza esse método (a remoção é feita por
 * remove(T)). Este experimento documenta o motivo.
 *
 * Execução: java -cp bin testes.ExperimentoPollLast
 */
public class ExperimentoPollLast {

    public static void main(String[] args) {
        LinkedList<String> lista = new LinkedList<String>();
        lista.addLast("A");
        lista.addLast("B");
        lista.addLast("C");
        System.out.println("Lista inicial: A, B, C");
        System.out.println("pollLast() devolveu: " + lista.pollLast() + "   (esperado: C)");
        System.out.println("Tamanho depois da chamada: " + lista.getSize() + "   (esperado: 2)");

        LinkedList<String> unica = new LinkedList<String>();
        unica.addLast("X");
        System.out.println("Lista com um único elemento: X");
        try {
            System.out.println("pollLast() devolveu: " + unica.pollLast() + "   (esperado: X)");
        } catch (NullPointerException e) {
            System.out.println("pollLast() lançou NullPointerException   (esperado: devolver X)");
        }
    }
}
