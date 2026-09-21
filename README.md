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

**Ambiente:** notebook com 16 núcleos lógicos · JDK 26 (preview habilitado) · 10 execuções por cenário.

Os tempos de cada uma das 520 execuções estão em [`resultados/experimentos_brutos.csv`](resultados/experimentos_brutos.csv). As tabelas abaixo mostram a **média das 10 execuções** de cada cenário, como pede a seção 9 do enunciado, e o intervalo entre a execução mais rápida e a mais lenta.

Speedup = tempo médio sequencial / tempo médio paralelo. "Resultado correto" compara cada execução com o sequencial usando tolerância relativa de 1e-9, pois somar em blocos altera as últimas casas decimais do `double`. Todas as 520 execuções deram resultado correto.

### Melhor quantidade de tarefas por implementação

#### E1 — Matriz 500 x 500

| Implementação | Tempo médio (ms) | Speedup | Quantidade de tarefas | Resultado correto |
|---|---:|---:|:---:|:---:|
| Sequencial | 3.219,2 | 1,00 | - | Sim |
| Paralelismo não estruturado | 512,2 | 6,28 | 100 | Sim |
| Paralelismo estruturado | 501,2 | 6,42 | 100 | Sim |
| Estruturado + variável atômica (DoubleAdder) | 493,2 | 6,53 | 100 | Sim |
| Estruturado + coleção concorrente (ConcurrentLinkedQueue) | 512,6 | 6,28 | 100 | Sim |

#### E2 — Matriz 1000 x 1000

| Implementação | Tempo médio (ms) | Speedup | Quantidade de tarefas | Resultado correto |
|---|---:|---:|:---:|:---:|
| Sequencial | 12.873,5 | 1,00 | - | Sim |
| Paralelismo não estruturado | 2.076,1 | 6,20 | 100 | Sim |
| Paralelismo estruturado | 1.955,4 | 6,58 | 100 | Sim |
| Estruturado + variável atômica (DoubleAdder) | 1.965,2 | 6,55 | 100 | Sim |
| Estruturado + coleção concorrente (ConcurrentLinkedQueue) | 1.953,4 | 6,59 | 100 | Sim |

#### E3 — Matriz 1500 x 1500

| Implementação | Tempo médio (ms) | Speedup | Quantidade de tarefas | Resultado correto |
|---|---:|---:|:---:|:---:|
| Sequencial | 28.943,2 | 1,00 | - | Sim |
| Paralelismo não estruturado | 4.324,4 | 6,69 | 100 | Sim |
| Paralelismo estruturado | 4.301,0 | 6,73 | 100 | Sim |
| Estruturado + variável atômica (DoubleAdder) | 4.278,2 | 6,77 | 100 | Sim |
| Estruturado + coleção concorrente (ConcurrentLinkedQueue) | 4.311,8 | 6,71 | 100 | Sim |

#### E4 — Matriz 2000 x 2000

| Implementação | Tempo médio (ms) | Speedup | Quantidade de tarefas | Resultado correto |
|---|---:|---:|:---:|:---:|
| Sequencial | 51.424,8 | 1,00 | - | Sim |
| Paralelismo não estruturado | 7.678,5 | 6,70 | 100 | Sim |
| Paralelismo estruturado | 7.601,5 | 6,77 | 100 | Sim |
| Estruturado + variável atômica (DoubleAdder) | 7.553,5 | 6,81 | 100 | Sim |
| Estruturado + coleção concorrente (ConcurrentLinkedQueue) | 7.679,8 | 6,70 | 100 | Sim |

### Tempo médio por quantidade de tarefas (ms, speedup entre parênteses)

| Implementação | Tarefas | E1 — 500 x 500 | E2 — 1000 x 1000 | E3 — 1500 x 1500 | E4 — 2000 x 2000 |
|---|:---:|---:|---:|---:|---:|
| Sequencial | - | 3.219,2 (1,00) | 12.873,5 (1,00) | 28.943,2 (1,00) | 51.424,8 (1,00) |
| Não estruturado | 5 | 752,2 (4,28) | 3.234,9 (3,98) | 6.620,5 (4,37) | 11.771,5 (4,37) |
| Não estruturado | 10 | 618,3 (5,21) | 2.478,3 (5,19) | 5.318,6 (5,44) | 9.376,6 (5,48) |
| Não estruturado | 100 | 512,2 (6,28) | 2.076,1 (6,20) | 4.324,4 (6,69) | 7.678,5 (6,70) |
| Estruturado | 5 | 805,5 (4,00) | 3.084,3 (4,17) | 6.842,7 (4,23) | 12.168,9 (4,23) |
| Estruturado | 10 | 611,7 (5,26) | 2.320,3 (5,55) | 5.075,3 (5,70) | 8.965,4 (5,74) |
| Estruturado | 100 | 501,2 (6,42) | 1.955,4 (6,58) | 4.301,0 (6,73) | 7.601,5 (6,77) |
| Estruturado + atômica | 5 | 818,2 (3,93) | 3.148,2 (4,09) | 6.909,3 (4,19) | 12.114,3 (4,24) |
| Estruturado + atômica | 10 | 596,9 (5,39) | 2.298,6 (5,60) | 5.039,2 (5,74) | 8.987,9 (5,72) |
| Estruturado + atômica | 100 | 493,2 (6,53) | 1.965,2 (6,55) | 4.278,2 (6,77) | 7.553,5 (6,81) |
| Estruturado + coleção | 5 | 834,2 (3,86) | 3.135,8 (4,11) | 6.988,4 (4,14) | 12.355,8 (4,16) |
| Estruturado + coleção | 10 | 589,7 (5,46) | 2.306,5 (5,58) | 5.098,0 (5,68) | 9.094,7 (5,65) |
| Estruturado + coleção | 100 | 512,6 (6,28) | 1.953,4 (6,59) | 4.311,8 (6,71) | 7.679,8 (6,70) |

### Variação entre as 10 execuções (ms, mais rápida – mais lenta)

| Implementação | Tarefas | E1 — 500 x 500 | E2 — 1000 x 1000 | E3 — 1500 x 1500 | E4 — 2000 x 2000 |
|---|:---:|---:|---:|---:|---:|
| Sequencial | - | 3.196,0 – 3.280,7 | 12.840,6 – 12.951,3 | 28.865,2 – 29.108,8 | 51.310,0 – 51.602,8 |
| Não estruturado | 5 | 715,8 – 856,4 | 2.960,8 – 3.543,0 | 6.546,3 – 6.723,9 | 11.579,6 – 12.090,3 |
| Não estruturado | 10 | 576,4 – 636,6 | 2.382,3 – 2.717,3 | 5.126,8 – 5.482,1 | 9.244,9 – 9.552,0 |
| Não estruturado | 100 | 490,6 – 563,3 | 1.924,6 – 2.333,8 | 4.299,1 – 4.373,9 | 7.572,5 – 7.755,6 |
| Estruturado | 5 | 732,7 – 846,9 | 3.023,2 – 3.169,1 | 6.737,1 – 6.975,1 | 11.440,2 – 12.418,8 |
| Estruturado | 10 | 571,5 – 639,7 | 2.277,4 – 2.389,3 | 5.007,3 – 5.156,6 | 8.802,4 – 9.032,3 |
| Estruturado | 100 | 493,0 – 510,0 | 1.914,2 – 2.033,8 | 4.253,9 – 4.358,3 | 7.577,3 – 7.652,9 |
| Estruturado + atômica | 5 | 796,3 – 845,6 | 3.022,3 – 3.238,9 | 6.791,9 – 7.010,9 | 11.991,9 – 12.310,7 |
| Estruturado + atômica | 10 | 581,8 – 621,2 | 2.254,8 – 2.383,8 | 4.930,2 – 5.152,8 | 8.847,7 – 9.383,9 |
| Estruturado + atômica | 100 | 483,8 – 500,4 | 1.910,9 – 2.102,9 | 4.230,1 – 4.326,6 | 7.477,9 – 7.627,8 |
| Estruturado + coleção | 5 | 804,4 – 874,4 | 3.048,3 – 3.240,0 | 6.878,2 – 7.135,6 | 12.164,2 – 12.531,4 |
| Estruturado + coleção | 10 | 584,9 – 595,7 | 2.260,7 – 2.410,5 | 5.055,9 – 5.167,4 | 9.014,4 – 9.164,4 |
| Estruturado + coleção | 100 | 486,4 – 572,2 | 1.896,8 – 2.117,6 | 4.275,3 – 4.359,4 | 7.626,9 – 7.822,7 |

## Análise comparativa dos resultados

Além do speedup, usamos a **eficiência**, que mostra quanto cada thread em uso contribui para o ganho:

> Eficiência = speedup ÷ quantidade de tarefas executando ao mesmo tempo

Com 5 e 10 tarefas, o divisor é o próprio número de tarefas. Com 100 tarefas, é o número de núcleos lógicos (16), porque as tarefas excedentes aguardam para executar. Eficiência de 100% significaria que cada thread rende exatamente o mesmo que o sequencial.

### 1. Análise de tempo das quatro versões (E4 — 2000 x 2000)

| Tarefas | V1 - Sequencial | V2 - Não estruturado | V3 - Estruturado | V4a - Atômica | V4b - Coleção |
|:---:|---:|---:|---:|---:|---:|
| - | 51.424,8 ms | | | | |
| 5 | | 11.771,5 ms | 12.168,9 ms | 12.114,3 ms | 12.355,8 ms |
| 10 | | 9.376,6 ms | 8.965,4 ms | 8.987,9 ms | 9.094,7 ms |
| 100 | | 7.678,5 ms | 7.601,5 ms | 7.553,5 ms | 7.679,8 ms |

#### Onde o tempo é gasto

| Etapa | O que acontece | Custo |
|---|---|---|
| Criar as tarefas | Dividir as linhas e chamar `submit` ou `fork` de 5 a 100 vezes | Microssegundos |
| Calcular | 4 milhões de chamadas de `calcular`, cada uma com 1.000 iterações de `sin`, `cos` e `sqrt` | Praticamente todo o tempo |
| Juntar os resultados | Somar de 5 a 100 resultados parciais, via `get()`, `join()`, `DoubleAdder` ou fila | Microssegundos |

O cálculo é idêntico nas quatro versões. As diferenças entre os modelos estão só na primeira e na última etapa, que juntas custam uma fração ínfima do total. Por isso o que determina o tempo é **quantas tarefas calculam ao mesmo tempo**, e não o modelo de paralelismo.

### 2. Influência da quantidade de tarefas

| Versão | 5 → 10 tarefas | 10 → 100 tarefas | 5 → 100 tarefas |
|---|---:|---:|---:|
| V2 - Não estruturado | −2.395 ms (−20,3%) | −1.698 ms (−18,1%) | −34,8% |
| V3 - Estruturado | −3.203 ms (−26,3%) | −1.364 ms (−15,2%) | −37,5% |
| V4a - Atômica | −3.126 ms (−25,8%) | −1.434 ms (−16,0%) | −37,6% |
| V4b - Coleção | −3.261 ms (−26,4%) | −1.415 ms (−15,6%) | −37,8% |

| Tarefas | V2 (speedup / eficiência) | V3 (speedup / eficiência) | V4a (speedup / eficiência) | V4b (speedup / eficiência) |
|:---:|---:|---:|---:|---:|
| 5 | 4,37x / 87% | 4,23x / 85% | 4,24x / 85% | 4,16x / 83% |
| 10 | 5,48x / 55% | 5,74x / 57% | 5,72x / 57% | 5,65x / 57% |
| 100 | 6,70x / 42% | 6,77x / 42% | 6,81x / 43% | 6,70x / 42% |

O padrão é o mesmo nas quatro versões: **o tempo cai com mais tarefas, mas a eficiência por thread também cai.**

- **Com 5 tarefas**, cada thread rende perto do ideal (83% a 87%), mas a maior parte dos núcleos fica sem trabalho.
- **De 5 para 10 tarefas**, o tempo cai de 20% a 26%, porque mais núcleos passam a calcular. Cada thread passa a render menos, já que as threads começam a disputar recursos compartilhados do processador, como cache e unidades de cálculo.
- **De 10 para 100 tarefas**, o ganho é menor, de 15% a 18%. As tarefas extras não trazem núcleos novos: os blocos ficam pequenos (20 linhas cada), todos os núcleos ficam ocupados e, quando um termina seu bloco, pega o próximo. Isso equilibra a carga, e nenhum núcleo fica ocioso esperando o mais lento.

**100 tarefas foi a melhor quantidade para todas as implementações, nos quatro tamanhos de matriz.** Aumentar o número de tarefas até ocupar todos os núcleos é o que mais reduz o tempo total, mesmo com perda de eficiência por thread.

### 3. Influência do tamanho da matriz

Speedup com 100 tarefas em cada tamanho:

| Versão | E1 — 500 | E2 — 1000 | E3 — 1500 | E4 — 2000 |
|---|---:|---:|---:|---:|
| V2 - Não estruturado | 6,28x | 6,20x | 6,69x | 6,70x |
| V3 - Estruturado | 6,42x | 6,58x | 6,73x | 6,77x |
| V4a - Atômica | 6,53x | 6,55x | 6,77x | 6,81x |
| V4b - Coleção | 6,28x | 6,59x | 6,71x | 6,70x |

O speedup cresce levemente com o tamanho da matriz. O custo de criar, coordenar e juntar as tarefas é praticamente fixo, enquanto o trabalho de cálculo cresce com o número de elementos. Em matrizes maiores, esse custo fixo pesa menos no total, e o ganho do paralelismo aparece mais inteiro. Mesmo na menor matriz (250 mil elementos), o trabalho já é grande o bastante para o paralelismo compensar com folga, ao contrário do exemplo da aula 2 com um laço pequeno, em que a versão paralela ficou mais lenta que a sequencial (0,7x).

### 4. Influência do modelo de paralelismo

Diferença de tempo em relação à V3, na E4 (2000 x 2000):

| Tarefas | V2 - Não estruturado | V4a - Atômica | V4b - Coleção |
|:---:|---:|---:|---:|
| 5 | −3,3% | −0,4% | +1,5% |
| 10 | +4,6% | +0,3% | +1,4% |
| 100 | +1,0% | −0,6% | +1,0% |

#### V2 (não estruturado) × V3 (estruturado)

As diferenças ficaram abaixo de 5%, sem que um modelo fosse sempre o mais rápido. O modelo de gerenciamento **não mudou o desempenho de forma significativa**, porque os dois executam o mesmo cálculo com o mesmo grau de paralelismo real:

- a V2 usa um pool de threads do sistema com uma thread por núcleo, e as tarefas excedentes aguardam na fila do executor;
- a V3 usa virtual threads, que rodam sobre um conjunto de threads do sistema também do tamanho do número de núcleos.

Virtual threads trazem ganho quando as tarefas passam tempo esperando (rede, disco, banco de dados), porque liberam a thread do sistema durante a espera. Como nosso cálculo só usa processador e nunca espera, esse benefício não aparece aqui.

Com 10 tarefas, a V2 ficou de 4% a 6% mais lenta que as versões estruturadas nas matrizes 1000, 1500 e 2000. É uma diferença pequena, mas consistente, que pode vir da forma como o executor e o escalonador de virtual threads distribuem as tarefas entre os núcleos. Os dados não permitem afirmar a causa com segurança.

O impacto do modelo estruturado está na **operação do código**:

| Aspecto | V2 - Não estruturado | V3 - Estruturado |
|---|---|---|
| Ciclo de vida das tarefas | Independente do método que as criou | Limitado ao bloco `try` do escopo |
| Encerramento | `shutdown()` manual no `finally` | Automático ao fechar o escopo |
| Falha de uma tarefa | As demais continuam executando | O escopo cancela as demais e o `join()` propaga o erro |
| Espera pelos resultados | Um `get()` por `Future` | Um único `join()` |
| Risco de tarefas órfãs ou vazamento de threads | Existe, se o encerramento for esquecido | Não existe |

Isso apareceu na prática: ao migrar a V4b de `ExecutorService` para `StructuredTaskScope`, o `shutdown()` e o `awaitTermination()` deixaram de ser necessários, e foi corrigida uma falha silenciosa. Como o `Future` não era guardado, uma exceção numa tarefa seria ignorada e o resultado sairia incompleto sem aviso.

#### V3 (sem estado compartilhado) × V4 (com estado compartilhado)

A V4a ficou a menos de 0,6% da V3, e a V4b a no máximo 1,5%. **O estado compartilhado não reduziu a eficiência.** O fator decisivo foi a **granularidade das escritas**: cada tarefa calcula o bloco inteiro numa variável local e escreve no estado compartilhado uma única vez. Com 100 tarefas, a V4a faz 200 operações atômicas (`add` e `incrementAndGet`) e a V4b faz 100 inserções na fila. Cada operação leva dezenas de nanossegundos, contra cerca de 7,6 segundos de cálculo.

Entre as duas variantes:

- **V4a (`DoubleAdder`)** acumula a soma diretamente, sem etapa final. Foi escolhido no lugar do `AtomicInteger` porque o resultado é `double`, e um inteiro descartaria as casas decimais. O `AtomicInteger` foi mantido para contar as tarefas concluídas.
- **V4b (`ConcurrentLinkedQueue`)** guarda cada resultado parcial e soma tudo no final, na thread principal. Usa um pouco mais de memória, mas preserva os resultados individuais.

Ambas são não bloqueantes, baseadas em CAS: uma thread nunca espera outra liberar o recurso. Se cada um dos 4 milhões de elementos fosse somado diretamente ao estado compartilhado, a disputa seria constante, como no exemplo da aula 2 com `synchronized`, em que o resultado ficou correto mas as threads passaram a executar em fila e o paralelismo se perdeu.

### 5. O que aumenta e o que diminui a eficiência

| Aumenta a eficiência | Diminui a eficiência |
|---|---|
| Dividir o problema em blocos independentes, sem comunicação entre tarefas | Poucas tarefas, deixando núcleos ociosos |
| Calcular com variáveis locais em cada tarefa | Escrever no estado compartilhado a cada elemento, gerando disputa |
| Número de tarefas suficiente para ocupar todos os núcleos | Usar `synchronized` ou locks, que obrigam as threads a esperar em fila |
| Blocos pequenos o bastante para equilibrar a carga | Blocos muito desiguais: o tempo total passa a depender da tarefa mais lenta |
| Estruturas não bloqueantes (`DoubleAdder`, `ConcurrentLinkedQueue`) | Tarefas pequenas demais, em que o custo de criar e coordenar threads supera o ganho |
| Uma única escrita por tarefa no estado compartilhado | Criar muito mais threads do sistema do que núcleos, com custo de memória e troca de contexto |

### 6. Síntese

| Versão | Impacto no desempenho (E4, 100 tarefas) | Impacto na operação | Quando usar |
|---|---|---|---|
| V1 - Sequencial | 51,4 s (1,00x) | Mais simples, sem concorrência | Problemas pequenos ou sem partes independentes |
| V2 - Não estruturado | 7,7 s (6,70x) | Exige gerenciar o executor manualmente; falhas podem passar despercebidas | Quando é preciso controlar diretamente o pool de threads |
| V3 - Estruturado | 7,6 s (6,77x) | Ciclo de vida delimitado, encerramento automático e propagação de falhas | Opção recomendada para dividir e juntar tarefas |
| V4a - Estruturado + atômica | 7,6 s (6,81x) | Estado compartilhado seguro, sem bloqueio | Quando as tarefas precisam acumular um valor comum |
| V4b - Estruturado + coleção | 7,7 s (6,70x) | Preserva cada resultado parcial | Quando os resultados individuais precisam ser guardados ou inspecionados |

1. **Paralelizar reduziu o tempo de 51,4 s para cerca de 7,6 s** na maior matriz, em todos os modelos.
2. **A quantidade de tarefas foi o fator que mais influenciou o tempo**: de 5 para 100 tarefas, todas as versões ficaram entre 35% e 38% mais rápidas.
3. **O modelo de paralelismo não mudou o tempo de forma significativa**: as quatro versões ficaram a poucos por cento umas das outras, porque todas executam o mesmo cálculo com o mesmo número de núcleos.
4. **A escolha do modelo deve se basear na estrutura do código**, e não no desempenho: as versões estruturadas entregam o mesmo tempo da não estruturada, com ciclo de vida delimitado, encerramento automático e propagação de falhas.

### Limitações

- Todos os experimentos rodaram num único notebook.
- Não foram descartadas execuções de aquecimento do JIT.
- A V2 usa um pool do tamanho do número de núcleos. Com um pool do tamanho da quantidade de tarefas, 100 tarefas criariam 100 threads do sistema operacional, e o resultado provavelmente seria diferente.
