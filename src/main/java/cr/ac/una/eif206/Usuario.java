package cr.ac.una.eif206;

public class Usuario {
        private String id;
        private String clave;
        private String rol;

        public Usuario() {
        }

        public Usuario(String id, String clave, String rol) {
            this.id = id;
            this.clave = clave;
            this.rol = rol;
        }

        // --- Getters y Setters ---

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getClave() {
            return clave;
        }

        public void setClave(String clave) {
            this.clave = clave;
        }

        public String getRol() {
            return rol;
        }

        public void setRol(String rol) {
            this.rol = rol;
        }

        // --- Métodos de la lógica de negocio ---

        public boolean login(String inputId, String inputClave) {
            return this.id.equals(inputId) && this.clave.equals(inputClave);
        }

        public void cambiarClave(String nuevaClave) {
            if (nuevaClave != null && !nuevaClave.trim().isEmpty()) {
                this.clave = nuevaClave;
            } else {
                throw new IllegalArgumentException("La nueva clave no puede estar vacía.");
            }
        }
}
