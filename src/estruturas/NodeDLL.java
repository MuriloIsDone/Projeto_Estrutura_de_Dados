package estruturas;

// Classe NodeDLL<T>: Node de uma lista duplamente encadeada
// genérica
// Autor1: Ivan Carlos Alcântara de Oliveira.
// Data da Criação: 10/10/2026. 15h.
public class NodeDLL<T extends Comparable<T>>{
	private T data;
	private	NodeDLL<T> left, right;
	
	public	NodeDLL() {
		this(null, null, null);
	}
	
	public NodeDLL(T data, NodeDLL<T> left, NodeDLL<T> right){
			this.data = data;
			this.left = left;
			this.right = right;
	};

	public T getData() { return data;};
	public NodeDLL<T> getLeft(){ return left;};
	public NodeDLL<T> getRight(){ return right;};
	public void setData(T data) { this.data = data;};
	public void setLeft(NodeDLL<T> left){ this.left = left; };
	public void setRight(NodeDLL<T> right){ this.right = right; };
}
