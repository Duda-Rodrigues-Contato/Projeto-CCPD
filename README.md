# Projeto-CCPD
Projeto da disciplina de Computação Concorrente, Paralela e Distribuída.  

### Alunos
- Maria Eduarda Rodrigues Moraes
- Matheus Melquiades Nunes

## Organização

```
Projeto-CCPD/
├── README.md
│
├── src/
│   ├── core/
│       ├── Main.java                             # Fluxo principal do programa.
│       ├── Calcular.java                         # Função "calcular".
│       ├── GerarMatriz.java                      # Função "gerarMatriz".
│       └── Menu.java                             # Menus de exibição do programa.
│
│   ├── Implementacaosequencial/
│       └── Sequencial.java                       # Funções da Implementação sequencial.
│
│   └── Paralelismo/
│       ├── Estadocompartilhado/
│           ├── ParalelismoAtomico.java           # Funções do Paralelismo Atômico.
│           └── ParalelismoConcorrente.java       # Funções do Paralelismo Concorrente.
│       ├── Estruturado/
│           └── ParalelismoEstruturado.java       # Funções do Paralelismo Estruturado.
│       └── NaoEstruturado/
│           └── ParalelismoNaoEstruturado.java    # Funções do Paralelismo Não Estruturado.
│
└── manual-utilizacao.md                          # Manual de utilização da aplicação 

```

### Como Executar

``` bash
javac --release 26 --enable-preview -d bin $(find src -name "*.java")
java --enable-preview -cp bin core.Main
```

Manual completo de uso em [`docs/manual-utilizacao.md`](manual-utilizacao.md).

## Conceitos de concorrência aplicados

| Conceito | Onde é usado |
|---|---|
| Threads | `ParalelismoNaoEstruturado` (via `ExecutorService`) |
| Paralelismo estruturado | `ParalelismoEstruturado` (via `StructuredTaskScope`) |
| Coleções concorrentes | `ParalelismoConcorrente` (`ConcurrentLinkedQueue`) |
| Variáveis atômicas | `ParalelismoAtomico` |

## Prevenção de Deadlock, Livelock e Starvation

*(preencher conforme a implementação final do grupo — este é um critério avaliado, 20% da nota)*

- **Deadlock**: descrever por que não ocorre (ex.: nenhuma tarefa aguarda lock mantido por outra; sem locks aninhados).
- **Livelock**: descrever por que não ocorre (ex.: sem retries que dependem do estado de outra tarefa).
- **Starvation**: descrever por que não ocorre (ex.: divisão de blocos é fixa e igualitária entre as tarefas, sem fila de prioridade).

## Resultados dos experimentos

**Ambiente:** notebook com 16 núcleos lógicos · JDK 26 (preview habilitado) · 10 execuções por cenário, tempo médio em milissegundos.

Speedup = tempo sequencial / tempo paralelo. "Resultado correto" compara com o sequencial usando tolerância relativa de 1e-9, pois somar em blocos altera as últimas casas decimais do `double`.

### Melhor quantidade de tarefas por implementação

#### Matriz 500 x 500

| Implementação | Tempo médio (ms) | Speedup | Quantidade de tarefas | Resultado correto |
|---|---:|---:|:---:|:---:|
| Sequencial | 6.185,9 ¹ | 1,00 | - | Sim |
| Paralelismo não estruturado | 1.990,2 ¹ | 3,11 | 10 | Sim |
| Paralelismo estruturado | 1.970,7 ¹ | 3,14 | 100 | Sim |
| Estruturado + variável atômica | 1.968,4 ¹ | 3,14 | 100 | Sim |
| Estruturado + coleção concorrente | 1.998,3 ¹ | 3,10 | 5 | Sim |

#### Matriz 1000 x 1000

| Implementação | Tempo médio (ms) | Speedup | Quantidade de tarefas | Resultado correto |
|---|---:|---:|:---:|:---:|
| Sequencial | 29.936,8 ¹ | 1,00 | - | Sim |
| Paralelismo não estruturado | 7.932,8 ¹ | 3,77 | 10 | Sim |
| Paralelismo estruturado | 7.852,2 ¹ | 3,81 | 100 | Sim |
| Estruturado + variável atômica | 7.933,1 ¹ | 3,77 | 5 | Sim |
| Estruturado + coleção concorrente | 8.135,5 ¹ | 3,68 | 10 | Sim |

#### Matriz 1500 x 1500

| Implementação | Tempo médio (ms) | Speedup | Quantidade de tarefas | Resultado correto |
|---|---:|---:|:---:|:---:|
| Sequencial | 72.115,4 ¹ | 1,00 | - | Sim |
| Paralelismo não estruturado | 17.621,0 ¹ | 4,09 | 10 | Sim |
| Paralelismo estruturado | 17.663,1 ¹ | 4,08 | 5 | Sim |
| Estruturado + variável atômica | 4.392,0 | 16,42 ² | 100 | Sim |
| Estruturado + coleção concorrente | 4.413,9 | 16,34 ² | 100 | Sim |

#### Matriz 2000 x 2000

| Implementação | Tempo médio (ms) | Speedup | Quantidade de tarefas | Resultado correto |
|---|---:|---:|:---:|:---:|
| Sequencial | 51.875,1 | 1,00 | - | Sim |
| Paralelismo não estruturado | 7.823,9 | 6,63 | 100 | Sim |
| Paralelismo estruturado | 7.790,5 | 6,66 | 100 | Sim |
| Estruturado + variável atômica | 12.231,1 | 4,24 | 5 ³ | Sim |
| Estruturado + coleção concorrente | 29.240,9 ¹ | 1,77 | 100 | Sim |

### Tempo médio por quantidade de tarefas (ms, speedup entre parênteses)

| Implementação | Tarefas | 500 x 500 | 1000 x 1000 | 1500 x 1500 | 2000 x 2000 |
|---|:---:|---:|---:|---:|---:|
| Sequencial | - | 6.185,9 (1,00) ¹ | 29.936,8 (1,00) ¹ | 72.115,4 (1,00) ¹ | 51.875,1 (1,00) |
| Não estruturado | 5 | 2.021,8 (3,06) ¹ | 7.990,1 (3,75) ¹ | 17.947,2 (4,02) ¹ | 12.112,2 (4,28) |
| Não estruturado | 10 | 1.990,2 (3,11) ¹ | 7.932,8 (3,77) ¹ | 17.621,0 (4,09) ¹ | 9.486,1 (5,47) |
| Não estruturado | 100 | 1.990,7 (3,11) ¹ | 8.083,0 (3,70) ¹ | 18.031,0 (4,00) ¹ | 7.823,9 (6,63) |
| Estruturado | 5 | 1.995,7 (3,10) ¹ | 8.023,7 (3,73) ¹ | 17.663,1 (4,08) ¹ | 12.729,4 (4,08) |
| Estruturado | 10 | 1.990,6 (3,11) ¹ | 8.202,3 (3,65) ¹ | 17.949,8 (4,02) ¹ | 9.350,3 (5,55) |
| Estruturado | 100 | 1.970,7 (3,14) ¹ | 7.852,2 (3,81) ¹ | 17.836,3 (4,04) ¹ | 7.790,5 (6,66) |
| Estruturado + atômica | 5 | 2.034,3 (3,04) ¹ | 7.933,1 (3,77) ¹ | 14.356,0 (5,02) ¹ | 12.231,1 (4,24) |
| Estruturado + atômica | 10 | 1.995,5 (3,10) ¹ | 8.027,8 (3,73) ¹ | 5.352,9 (13,47) ² | 12.789,7 (4,06) ¹ |
| Estruturado + atômica | 100 | 1.968,4 (3,14) ¹ | 7.953,0 (3,76) ¹ | 4.392,0 (16,42) ² | 31.388,2 (1,65) ¹ |
| Estruturado + coleção | 5 | 1.998,3 (3,10) ¹ | 8.286,3 (3,61) ¹ | 7.140,5 (10,10) ² | 31.857,1 (1,63) ¹ |
| Estruturado + coleção | 10 | 2.048,4 (3,02) ¹ | 8.135,5 (3,68) ¹ | 5.299,1 (13,61) ² | 31.739,7 (1,63) ¹ |
| Estruturado + coleção | 100 | 2.001,8 (3,09) ¹ | 8.247,0 (3,63) ¹ | 4.413,9 (16,34) ² | 29.240,9 (1,77) ¹ |
