package estruturas;

public class Pilha <T> {   // classe do prof tb, deixa assim
	private static final int TAM_DEFAULT = 100; //defini um tamanho padrao para a pilha
	private	int topoPilha; // indice do topo, começa em -1 pq ta vazia
    private	T e[ ];
   	public	Pilha(int tamanho) {  // Nosso primeiro construtor, amem..
   		this.e = (T[]) new Object[tamanho];
   		this.topoPilha = -1;
   	}
  	public	Pilha() {// construtor 2
   		this(TAM_DEFAULT);
   	}   	
   	//Verifica se a pilha
   	//está vazia
   	public boolean isEmpty() {
   		return this.topoPilha == -1;	
   	}
   	//verifica se a pilha está
   	//cheia
   	public boolean isFull() {
   	  return this.topoPilha == this.e.length-1;
   	}
   	//insere um elemento e 
   	//no topo da pilha
   	public void push(T e) throws Exception{
   		if (! this.isFull( ))
   			this.e[++this.topoPilha] = e; // ja incrementa e usa o indice novo
   		else 
   			throw new Exception("overflow - Estouro de Pilha");
   	}
   	//remove um elemento 
   	//do topo da pilha
   	public T pop() throws Exception{
   		if (! this.isEmpty( ))
   			return this.e[this.topoPilha--]; // pega o de cima e so dps desce o indice
   		else{
   			throw new Exception( "underflow - Esvaziamento de Pilha");
   		}  
   	}
   	//retorna o elemento que está
   	//no topo da pilha
   	public T topo() throws Exception{
	  if ( ! this.isEmpty( ))
			return this.e[this.topoPilha];
		else{
			throw new Exception("Underlow - Esvaziamento de Pilha");
		}
   	}
   	//pega o total de elementos 
   	//armazenados na Pilha
   	public int sizeElements() {
	  return topoPilha+1; // +1 pq o indice começa em -1
   	}
   	   
	// Sobrescrita/sobreposição (override) do método toString(), que veio da superclasse Object.
	// O retorno do método toString() é a representação de um objeto em formato string, e toString()
	// geralmente é executado (de forma implícita) quando passamos um objeto ao System.out.print*().
	// Experimente incluir o seguinte código na main() e veja a saída:
	// Pilha p = new Pilha();
	// System.out.println(p);
	//
	// Depois, remova/comente o método toString() abaixo e rode o código acima novamente.
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Pilha] topoPilha: ")
			.append(topoPilha)
			.append(", capacidade: ")
			.append(e.length)
			.append(", quantidade de elementos: ")
			.append(sizeElements());
		if (topoPilha != -1) {
			sb.append(", valor do Topo: ")
				.append(this.e[this.topoPilha]);
		}else sb.append(", valor do Topo: PILHA VAZIA");

		sb.append("\nConteudo da Pilha': [ ");
			
		for (int i = 0; i <= topoPilha; ++i)
			if (i != topoPilha) sb.append( e[i] + ", ");
			else sb.append( e[i] );
		sb.append( " ]");
		return sb.toString();
	}

}
