#!/bin/sh
# Verifica se os testes automatizados detectam defeitos: introduz, em uma CÓPIA
# do código, quatro defeitos deliberados e confirma que a suíte reprova.
# Uso: sh verificar_mutacoes.sh   (executar na raiz do projeto; requer javac e java)
set -e
RAIZ=$(pwd)
TMP=$(mktemp -d)

mutar() {   # $1 descrição | $2 arquivo | $3 expressão do sed
    rm -rf "$TMP/src" "$TMP/bin"
    cp -r "$RAIZ/src" "$TMP/src"
    sed -i "$3" "$TMP/src/$2"
    javac -encoding UTF-8 -d "$TMP/bin" $(find "$TMP/src" -name "*.java")
    echo "### Defeito introduzido: $1"
    java -cp "$TMP/bin" testes.TestesB2 | grep -E "REPROVADO|Reprovados" | sed 's/ (.*//' || true
    echo
}

mutar "limite de anti-inanição alterado de 3 para 2" service/ControleFilas.java \
    's/LIMITE_ATENDIMENTOS_ALTA_SEGUIDOS = 3/LIMITE_ATENDIMENTOS_ALTA_SEGUIDOS = 2/'
mutar "desfazer devolve a solicitação ao FIM da fila" service/ControleFilas.java \
    's/enfileirar(auxiliar, s);  .*/\/\/ removido/;s/altaSeguidas = contadorAnterior;/enfileirar(destino, s); altaSeguidas = contadorAnterior;/'
mutar "desfazer não restaura o contador de anti-inanição" service/ControleFilas.java \
    's/altaSeguidas = contadorAnterior;/altaSeguidas = 0;/'
mutar "validação de texto vazio desligada" service/CentralAtendimento.java \
    's/if (valor == null || valor.trim().isEmpty()) {/if (false) {/'
rm -rf "$TMP"
