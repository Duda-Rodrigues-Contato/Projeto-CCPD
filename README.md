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

*(tabela de tempo médio, speedup, quantidade de tarefas e corretude do resultado — ver `docs/` ou seção de experimentos, conforme execução do `Experimento.java` ou testes manuais)*