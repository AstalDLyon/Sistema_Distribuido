# Projeto de Sistema Distribuído

## 📌 Arquitetura baseada nos princípios SOLID

Cada classe possui uma responsabilidade única e bem definida:

- **MainClient / MainServer** – Pontos de entrada do sistema
- **nomeServidor** – Lógica principal do servidor
- **Resolver** – Cliente e comunicação via sockets
- **FileManager** – Persistência de dados em arquivo
- **ConfigManager** – Configurações globais (Singleton, Thread-safe)
- **LogManager** – Classe que gerencia logs

---

## ✅ Funcionalidades

- [x] Registro de IP com nome  
- [x] Consulta com sockets  
- [x] Persistência em arquivo  
- [x] Replicação entre servidores (servidor distribuído)  
- [x] Configurações com conformidade Thread-safe  
- [x] ExecutorService para controlar acessos simultâneos  
- [x] Timeouts  
- [x] Logs

---
# 🧪 Tutorial de Teste dos Servidores Secundários

Após iniciar o **MainServer** (sem parâmetros), siga os passos abaixo para rodar os servidores secundários:

---

## ✅ Passo a Passo

### 1. Acesse o diretório do seu projeto

```cd C:\Users\bral\IdeaProjects\Sistema_Distribuido```

Substitua o caminho acima pelo diretório real do seu projeto.

### 2. Compile o projeto (caso ainda não tenha compilado)

```javac -d . src/main/java/com/av2/sistemadistribuidos/*.java```


### 3. Execute o servidor secundário
 ```java com.av2.sistemadistribuidos.MainServer secundario 12346```


### 4. Em outro terminal, execute outro servidor secundário

 ```cd C:\Users\bral\IdeaProjects\Sistema_Distribuido```
 ```java com.av2.sistemadistribuidos.MainServer secundario 12347```


### 5. Verifique se os servidores estão ativos

 ```netstat -ano | findstr "12345 12346 12347"```

### A saída esperada deve mostrar algo semelhante a:

   TCP 0.0.0.0:12345 0.0.0.0:0 LISTENING 4980

   TCP 0.0.0.0:12346 0.0.0.0:0 LISTENING 13516

   TCP 0.0.0.0:12347 0.0.0.0:0 LISTENING 10224

   TCP [::]:12345 [::]:0 LISTENING 4980

   TCP [::]:12346 [::]:0 LISTENING 13516

   TCP [::]:12347 [::]:0 LISTENING 10224


Este projeto é parte de um trabalho acadêmico, e novas funcionalidades serão adicionadas ao longo do tempo.
