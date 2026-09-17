# Manual de Utilização — Projeto-CCPD

Este manual explica como compilar, executar e utilizar a aplicação, incluindo como selecionar cada uma das implementações (sequencial, paralelismo não estruturado, paralelismo estruturado e estado compartilhado).

## Pré-requisitos

- JDK 21 ou superior instalado (necessário para `StructuredTaskScope`, que é uma *preview feature*).
- Verificar a versão instalada:

```bash
java -version
```

## Compilação

A partir da raiz do projeto (pasta `Projeto-CCPD/`):

```bash
javac --release 21 --enable-preview -d bin $(find src -name "*.java")
```

- `--enable-preview`: necessário porque `StructuredTaskScope` ainda é *preview* no JDK 21.
- `-d bin`: coloca os `.class` compilados numa pasta `bin/`, respeitando a estrutura de pacotes.

Se a compilação terminar sem mensagens de erro, os arquivos `.class` estarão em `bin/`.

## Execução

```bash
java --enable-preview -cp bin core.Main
```

Isso abre o menu principal da aplicação.

## Utilizando o menu

Ao iniciar, o programa exibe as opções de tamanho de matriz disponíveis:

```
=================================
1 - Matriz 500 x 500  
2 - Matriz 1000 x 1000
3 - Matriz 1500 x 1500
4 - Matriz 2000 x 2000
0 - Exit
```

Após escolher o tamanho da matriz, o programa solicita:

1. **Qual implementação executar**, dentre:
   - Sequencial
   - Paralelismo não estruturado
   - Paralelismo estruturado
   - Estado compartilhado (variável atômica)
   - Estado compartilhado (coleção concorrente)
2. **Quantidade de tarefas** (obrigatório para todas as versões paralelas — ignorado na sequencial). Valores sugeridos para os experimentos: `5`, `10` ou `100`.

Ao final da execução, o programa exibe:

```
Resultado: <valor calculado>
Tempo: <tempo em ms>
```

## Exemplo de uso — comparando implementações

Para comparar o desempenho manualmente, execute a mesma matriz com implementações diferentes e anote os tempos:

``` Exemplo 1:
1 → escolhe matriz 500x500
→ escolhe "Sequencial"
→ Resultado: 123456.789012 | Tempo: 842.311 ms
```

``` Exemplo 2:
1 → escolhe matriz 500x500
→ escolhe "Paralelismo estruturado"
→ informa 10 tarefas
→ Resultado: 123456.789012 | Tempo: 213.045 ms
```

O **resultado** deve ser o mesmo (ou muito próximo, dada a natureza de soma em ponto flutuante) entre todas as implementações para a mesma matriz — isso confirma a corretude da versão paralela.

## Executando o benchmark completo (opcional)

Caso o grupo tenha implementado o `Experimento.java`, é possível rodar automaticamente todas as combinações de tamanho de matriz × quantidade de tarefas × implementação, 10 repetições cada, gerando a tabela final:

```bash
java --enable-preview -cp bin Benchmark.Experimento
```

O resultado é salvo em `resultados/tabela_final.csv`, com tempo médio, speedup e verificação de corretude para cada combinação.

> A pasta `resultados/` deve existir previamente na raiz do projeto.

## Solução de problemas comuns

| Problema | Causa provável | Solução |
|---|---|---|
| `Error: Could not find or load main class` | Compilação não gerou o `.class`, ou está rodando a classe errada | Verificar se `javac` terminou sem erros e se está apontando para `core.Main` |
| `preview features are not enabled` | Faltou a flag `--enable-preview` na compilação ou execução | Adicionar `--enable-preview` em ambos os comandos |
| Resultado diferente entre versões | Erro de lógica na divisão de blocos, ou condição de corrida não tratada | Revisar a divisão de linhas por tarefa e o uso do estado compartilhado |
| Travamento (sem saída, sem erro) | Possível deadlock — verificar se alguma tarefa está esperando por outra que nunca libera um recurso | Revisar uso de locks/synchronized, se houver |