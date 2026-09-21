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

As quatro versões foram projetadas para que esses problemas não possam ocorrer, e não apenas para que sejam improváveis. A base disso é a forma como o trabalho é dividido: cada tarefa processa um bloco fixo de linhas usando apenas variáveis locais, a matriz é somente lida, e o estado compartilhado (V4) é escrito uma única vez por tarefa, com estruturas não bloqueantes.

### Deadlock

Um deadlock exige que threads **segurem um recurso enquanto esperam por outro**, formando uma espera circular. Nenhuma das condições para isso existe no projeto:

- **Não há locks.** Nenhuma versão usa `synchronized`, `Lock` ou semáforos. O estado compartilhado usa `DoubleAdder`, `AtomicInteger` (V4a) e `ConcurrentLinkedQueue` (V4b), que são operações atômicas baseadas em CAS: uma thread nunca fica bloqueada segurando algo que outra precisa.
- **As tarefas não esperam umas pelas outras.** Cada tarefa apenas calcula seu bloco e termina. Nenhuma submete subtarefas ao mesmo executor e espera por elas, o que evita o deadlock clássico de pool de threads.
- **A espera é em uma única direção.** Só a thread principal espera: por `future.get()` na V2 e por `scope.join()` nas versões estruturadas. As tarefas nunca esperam pela thread principal, então não há ciclo.
- **A espera sempre termina.** Cada tarefa tem uma quantidade finita de trabalho. Se uma falhar, na V2 o `get()` lança `ExecutionException` e o `finally` encerra o executor; nas versões estruturadas, o `StructuredTaskScope` cancela as demais subtarefas e o `join()` propaga a falha.

### Livelock

Um livelock ocorre quando threads continuam ativas, mas **ficam reagindo umas às outras sem progredir**, como em tentativas repetidas que dependem do estado de outra thread.

- **Não há laços de nova tentativa no código.** Nenhuma tarefa verifica o estado de outra para decidir o que fazer, e não existe lógica de "tentar de novo".
- **A única repetição é interna às classes atômicas.** Quando um CAS falha, é porque outra thread concluiu sua escrita com sucesso. Ou seja, toda falha significa progresso de alguém, e o sistema como um todo sempre avança.
- **A disputa é mínima.** Cada tarefa escreve no estado compartilhado uma única vez, depois de calcular o bloco inteiro localmente. Com 5, 10 ou 100 tarefas, são no máximo 100 escritas concorrentes, contra centenas de milhares de cálculos independentes.

### Starvation

Starvation ocorre quando uma thread **nunca consegue os recursos de que precisa** para executar, por exemplo por ter prioridade menor ou perder sempre a disputa por um lock.

- **A divisão é fixa e igualitária.** Todas as tarefas recebem `linhas / tarefas` linhas, e a última recebe as que sobrarem da divisão. Nenhuma tarefa compete por trabalho ou depende da ordem de execução para receber sua parte.
- **Não há prioridades.** Todas as threads usam a prioridade padrão, e nenhuma fila favorece uma tarefa em relação às outras.
- **Toda tarefa enfileirada é executada.** Na V2, as tarefas excedentes aguardam na fila do `ExecutorService`, que é atendida em ordem de chegada. Nas versões estruturadas, as virtual threads são distribuídas entre as threads do sistema pelo escalonador da JVM. Como cada tarefa termina e libera sua thread, as que estão aguardando sempre chegam a executar.
- **Não há lock para disputar.** Sem locks, nenhuma thread pode ser preterida repetidamente no acesso a um recurso.

### Resumo

| Problema | Condição necessária | Por que não ocorre |
|---|---|---|
| Deadlock | Recursos retidos + espera circular | Sem locks; tarefas independentes; só a thread principal espera |
| Livelock | Tentativas repetidas dependentes de outras threads | Sem laços de nova tentativa; falha de CAS implica progresso de outra thread |
| Starvation | Acesso desigual a recursos ou prioridades | Blocos fixos e iguais; sem prioridades; filas atendidas em ordem |

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

### Observações sobre a medição

¹ **Medido com o notebook em estado de desempenho reduzido** (provável economia de energia ou redução térmica da CPU). Nesses trechos todas as versões, inclusive a sequencial, ficaram de 2 a 4 vezes mais lentas, e os tempos deixaram de variar com a quantidade de tarefas. Os valores não representam o desempenho real das implementações.

² **Speedup distorcido para cima.** O tempo da versão paralela foi medido com o notebook em estado normal, mas o sequencial da mesma matriz rodou no estado reduzido, inflando a razão.

³ Com 10 e 100 tarefas, esta versão foi afetada pelo estado reduzido (ver tabela detalhada), por isso a melhor medição válida foi a de 5 tarefas.

Os cenários sem marcação, principalmente sequencial, não estruturado e estruturado na matriz 2000 x 2000, foram medidos inteiramente no estado normal e são a referência mais confiável de desempenho.
