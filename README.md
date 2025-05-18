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
- [ ] Replicação entre servidores (servidor distribuído)  
- [x] Configurações com conformidade Thread-safe  
- [x] ExecutorService para controlar acessos simultâneos  
- [x] Timeouts  
- [x] Logs  
- [ ] Consulta distribuída com resposta do primeiro servidor disponível  

---

Este projeto é parte de um trabalho acadêmico, e novas funcionalidades serão adicionadas ao longo do tempo.
