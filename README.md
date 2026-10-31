# Relatório Técnico - SAES CNES

**1. Identificação**
* **Projeto:** Sistema de Análise de Estabelecimentos de Saúde (SAES CNES)
* **Disciplina:** Programação Avançada Orientada a Objetos
* **Grupo:** 01
* **Data:** 31/10/2026

**2. Problema e Objetivos**
A gestão descentralizada do SUS exige clareza sobre os estabelecimentos disponíveis. O SAES foi desenvolvido para importar, mapear e analisar a rede de atendimento a partir de dados oficiais do MS. 

**3. Fonte Oficial**
A extração do conjunto oficial de dados foi realizada por meio de ferramentas de linha de comando via **FTP** (`ftp.datasus.gov.br`), acessando diretamente os repositórios públicos do DATASUS. O download ocorreu em ambiente **Linux Xubuntu**. 

**4. Compreensão dos Dados**
Mapeamos os campos: `codigo_cnes`, `nome_fantasia`, `uf`, `municipio`, `tipo_estabelecimento`, `esfera_administrativa` e `atende_sus`. A conversão de `SIM/NAO` para valores booleanos garante a tipagem forte exigida no modelo.

**5. Tratamento e Amostragem**
Devido à capacidade de hardware restrita do ambiente de desenvolvimento (aproximadamente 3.7 GiB de memória RAM e swap), realizar o carregamento do dataset integral na memória tornou-se inviável, causando estouros de heap. Assim, o grupo estruturou uma amostra programática representativa de 500 registros. A limpeza inclui aparar espaços e rejeitar linhas com menos de 7 colunas delimitadas por `;`.

**6. Solução e Arquitetura**
Projeto desenvolvido em ecossistema Java. O gerenciamento de dependências foi orquestrado via **Maven**. Utiliza-se Spring Boot (REST) integrando JPA/Hibernate para persistência no PostgreSQL da Neon. 
* **Controller:** Expõe métodos GET, POST, PUT, DELETE, mapeando HTTP para ações.
* **Service:** Encapsula regras de importação de CSV `multipart/form-data`.
* **Repository:** Executa Queries nativas e dinâmicas (filtros).

**7. Dicionário da Entidade**
* `id` (Long, BD auto)
* `codigoCnes` (String, obrigatório)
* `nomeEstabelecimento` (String)
* `uf` (String, max 2)
* `atendeSus` (Boolean)

**8. API REST**
A API responde no path `/api/estabelecimentos`. Retorna 200/201 (Sucesso), 404 (ID não encontrado) e 400 (Má requisição em falhas JSON).

**9. Decisões Técnicas**
Utilizamos a classe `Optional` nativa do Java nos Controllers para evitar NullPointerExceptions nas pesquisas por ID, retornando NotFound apropriadamente.

**12. Dificuldades**
A principal limitação enfrentada foi a gestão de memória ao parsear arquivos massivos, superada com a adoção da amostragem controlada de registros. A resolução de dependências no arquivo pom.xml usando Maven foi padronizada para prevenir vulnerabilidades transitórias durante a build.
