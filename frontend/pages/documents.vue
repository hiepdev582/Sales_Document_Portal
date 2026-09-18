<template>
  <div>
    <!-- Header Controls -->
    <div
      class="glass-panel"
      style="
        padding: 1.5rem 2rem;
        margin-bottom: 2rem;
        display: flex;
        justify-content: space-between;
        align-items: center;
      "
    >
      <div>
        <h2 style="font-size: 1.5rem; font-weight: 800">
          Kho Tài Liệu Hợp Đồng
        </h2>
        <p style="font-size: 0.875rem; color: var(--text-muted)">
          Mã hóa AES-256-GCM at Rest & Bảo vệ Phân quyền ABAC/BFLA
        </p>
      </div>

      <div style="display: flex; gap: 1rem">
        <button
          @click="openUploadModal"
          class="glass-button"
          style="display: flex; align-items: center; gap: 0.5rem"
        >
          <svg
            width="18"
            height="18"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
            <polyline points="17 8 12 3 7 8" />
            <line x1="12" y1="3" x2="12" y2="15" />
          </svg>
          Tải lên Hợp đồng Mã hóa
        </button>
      </div>
    </div>

    <!-- Security Info Banner -->
    <div
      class="glass-panel"
      style="
        padding: 1.25rem;
        margin-bottom: 2rem;
        background: rgba(99, 102, 241, 0.08);
        border-color: rgba(99, 102, 241, 0.2);
      "
    >
      <div style="display: flex; gap: 1rem; align-items: flex-start">
        <div
          style="
            color: var(--accent-primary);
            font-size: 1.25rem;
            line-height: 1;
          "
        >
          🛡️
        </div>
        <div style="font-size: 0.85rem; color: var(--text-main)">
          <strong>Quy tắc bảo mật ABAC đang thực thi:</strong> Tài liệu cấp độ
          <code>CONFIDENTIAL</code> chỉ có
          <strong>Chủ sở hữu (Owner)</strong> hoặc <strong>ADMIN</strong> mới
          thấy và tải về. Tài liệu <code>INTERNAL</code> chỉ hiển thị cho người
          dùng cùng phòng ban (<strong>{{ user?.department }}</strong
          >).
        </div>
      </div>
    </div>

    <!-- Alert Message -->
    <div
      v-if="alertMessage"
      :style="{
        background:
          alertType === 'error'
            ? 'rgba(239, 68, 68, 0.15)'
            : 'rgba(16, 185, 129, 0.15)',
        borderColor:
          alertType === 'error'
            ? 'rgba(239, 68, 68, 0.3)'
            : 'rgba(16, 185, 129, 0.3)',
        color: alertType === 'error' ? '#f87171' : '#6ee7b7',
      }"
      style="
        border: 1px solid;
        padding: 0.75rem 1.25rem;
        border-radius: 12px;
        margin-bottom: 1.5rem;
        display: flex;
        justify-content: space-between;
        align-items: center;
      "
    >
      <span>{{ alertMessage }}</span>
      <button
        @click="alertMessage = ''"
        style="
          background: none;
          border: none;
          color: inherit;
          cursor: pointer;
          font-size: 1.2rem;
        "
      >
        &times;
      </button>
    </div>

    <!-- Documents Table -->
    <div class="glass-panel" style="overflow: hidden">
      <div
        v-if="loading"
        style="padding: 3rem; text-align: center; color: var(--text-muted)"
      >
        Đang tải danh sách tài liệu...
      </div>

      <div
        v-else-if="documents.length === 0"
        style="padding: 4rem 2rem; text-align: center; color: var(--text-muted)"
      >
        <div style="font-size: 2.5rem; margin-bottom: 1rem">📂</div>
        <p style="font-weight: 600; font-size: 1.1rem; color: var(--text-main)">
          Chưa có tài liệu nào khả dụng
        </p>
        <p style="font-size: 0.875rem; margin-top: 0.25rem">
          Hãy tải lên tài liệu mới hoặc kiểm tra lại quyền phòng ban của bạn.
        </p>
      </div>

      <table
        v-else
        style="
          width: 100%;
          border-collapse: collapse;
          text-align: left;
          font-size: 0.9rem;
        "
      >
        <thead>
          <tr
            style="
              border-bottom: 1px solid var(--border-card);
              background: rgba(255, 255, 255, 0.02);
            "
          >
            <th style="padding: 1rem 1.5rem">ID</th>
            <th style="padding: 1rem 1.5rem">Tên Tài Liệu / Tệp gốc</th>
            <th style="padding: 1rem 1.5rem">Phòng ban</th>
            <th style="padding: 1rem 1.5rem">Cấp độ Bảo mật</th>
            <th style="padding: 1rem 1.5rem">Người sở hữu</th>
            <th style="padding: 1rem 1.5rem; text-align: right">Thao tác</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="doc in documents"
            :key="doc.id"
            style="
              border-bottom: 1px solid var(--border-card);
              transition: background 0.15s ease;
            "
            class="doc-row"
          >
            <td
              style="
                padding: 1rem 1.5rem;
                font-family: monospace;
                color: var(--text-muted);
              "
            >
              #{{ doc.id }}
            </td>
            <td style="padding: 1rem 1.5rem">
              <!-- Vue Template Escaping automatically prevents Stored XSS -->
              <div style="font-weight: 700; color: #fff">{{ doc.title }}</div>
              <div style="font-size: 0.75rem; color: var(--text-muted)">
                {{ doc.originalFilename }} ({{ formatSize(doc.fileSize) }})
              </div>
            </td>
            <td style="padding: 1rem 1.5rem">
              <span class="badge badge-internal">{{ doc.department }}</span>
            </td>
            <td style="padding: 1rem 1.5rem">
              <span
                :class="['badge', getClassificationBadge(doc.classification)]"
                >{{ doc.classification }}</span
              >
            </td>
            <td style="padding: 1rem 1.5rem">
              <!-- Escape rendered text -->
              <div style="font-weight: 500">{{ doc.ownerName }}</div>
              <div
                v-if="doc.ownerId === user?.id"
                style="
                  font-size: 0.7rem;
                  color: var(--accent-primary);
                  font-weight: 700;
                "
              >
                (Chủ sở hữu)
              </div>
            </td>
            <td style="padding: 1rem 1.5rem; text-align: right">
              <div
                style="display: flex; gap: 0.5rem; justify-content: flex-end"
              >
                <button
                  @click="downloadDoc(doc)"
                  class="glass-button-outline"
                  style="padding: 0.4rem 0.8rem; font-size: 0.8rem"
                >
                  ⬇️ Tải xuống
                </button>

                <!-- BFLA Protection: Backend enforces @PreAuthorize/Abac, UI conditionally renders for UX -->
                <button
                  v-if="user?.role === 'ROLE_ADMIN' || doc.ownerId === user?.id"
                  @click="deleteDoc(doc.id)"
                  class="glass-button-outline"
                  style="
                    padding: 0.4rem 0.8rem;
                    font-size: 0.8rem;
                    border-color: rgba(239, 68, 68, 0.4);
                    color: #f87171;
                  "
                >
                  🗑️ Xóa
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Direct BOLA IDOR Test Panel -->
    <div class="glass-panel" style="margin-top: 2rem; padding: 1.5rem">
      <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.5rem">
        🧪 Security Lab: BOLA (IDOR) & XSS Test Console
      </h3>
      <p
        style="
          font-size: 0.85rem;
          color: var(--text-muted);
          margin-bottom: 1rem;
        "
      >
        Thử nghiệm thay đổi ID tài liệu tùy ý trên API để kiểm tra phản hồi từ
        Backend Services.
      </p>

      <div
        style="display: flex; gap: 1rem; align-items: center; max-width: 600px"
      >
        <input
          v-model="testDocId"
          type="number"
          class="glass-input"
          placeholder="Nhập Document ID (ví dụ: 101)"
        />
        <button
          @click="testBolaAccess"
          class="glass-button-outline"
          style="white-space: nowrap"
        >
          Thử truy cập API Direct (/api/documents/{id})
        </button>
      </div>
    </div>

    <!-- Upload Modal -->
    <div
      v-if="showModal"
      style="
        position: fixed;
        inset: 0;
        background: rgba(0, 0, 0, 0.7);
        backdrop-filter: blur(8px);
        display: flex;
        align-items: center;
        justify-content: center;
        z-index: 100;
        padding: 1rem;
      "
    >
      <div
        class="glass-panel"
        style="width: 100%; max-width: 500px; padding: 2rem"
      >
        <h3 style="font-size: 1.35rem; font-weight: 800; margin-bottom: 1.5rem">
          Tải lên Tài liệu Hợp đồng
        </h3>

        <form
          @submit.prevent="handleUpload"
          style="display: flex; flex-direction: column; gap: 1.25rem"
        >
          <div>
            <label
              style="
                display: block;
                font-size: 0.85rem;
                font-weight: 600;
                margin-bottom: 0.5rem;
              "
              >Tiêu đề Tài liệu / Hợp đồng</label
            >
            <input
              v-model="uploadForm.title"
              type="text"
              class="glass-input"
              placeholder="Ví dụ: Hợp đồng Đại lý Sales Q3 (Thử chèn XSS payload)"
              required
            />
          </div>

          <div>
            <label
              style="
                display: block;
                font-size: 0.85rem;
                font-weight: 600;
                margin-bottom: 0.5rem;
              "
              >Chọn tệp tin hợp đồng</label
            >
            <input
              @change="onFileSelected"
              type="file"
              class="glass-input"
              required
            />
          </div>

          <div style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem">
            <div>
              <label
                style="
                  display: block;
                  font-size: 0.85rem;
                  font-weight: 600;
                  margin-bottom: 0.5rem;
                "
                >Phòng ban sở hữu</label
              >
              <select v-model="uploadForm.department" class="glass-input">
                <option value="SALES">SALES</option>
                <option value="MARKETING">MARKETING</option>
                <option value="FINANCE">FINANCE</option>
                <option value="IT">IT</option>
                <option value="EXECUTIVE">EXECUTIVE</option>
              </select>
            </div>

            <div>
              <label
                style="
                  display: block;
                  font-size: 0.85rem;
                  font-weight: 600;
                  margin-bottom: 0.5rem;
                "
                >Bảo mật (Classification)</label
              >
              <select v-model="uploadForm.classification" class="glass-input">
                <option value="PUBLIC">PUBLIC</option>
                <option value="INTERNAL">INTERNAL</option>
                <option value="CONFIDENTIAL">CONFIDENTIAL</option>
              </select>
            </div>
          </div>

          <div
            style="
              display: flex;
              gap: 1rem;
              justify-content: flex-end;
              margin-top: 1rem;
            "
          >
            <button
              type="button"
              @click="showModal = false"
              class="glass-button-outline"
            >
              Hủy
            </button>
            <button type="submit" class="glass-button" :disabled="uploading">
              {{ uploading ? "Đang mã hóa & Upload..." : "Mã hóa AES & Lưu" }}
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
const { accessToken, user } = useAuth();
const documents = ref<any[]>([]);
const loading = ref(true);
const uploading = ref(false);
const showModal = ref(false);
const selectedFile = ref<File | null>(null);

const alertMessage = ref("");
const alertType = ref<"success" | "error">("success");

const testDocId = ref<number | null>(null);

const uploadForm = reactive({
  title: "",
  department: "SALES",
  classification: "INTERNAL",
});

// Protected Page Middleware Check
onMounted(async () => {
  if (!user.value) {
    navigateTo("/login");
    return;
  }
  await fetchDocuments();
});

const fetchDocuments = async () => {
  loading.value = true;
  try {
    const data = await $fetch<any[]>("http://localhost:8080/api/documents", {
      headers: { Authorization: `Bearer ${accessToken.value}` },
    });
    documents.value = data;
  } catch (e: any) {
    showAlert(
      "Không thể tải danh sách tài liệu: " + (e.data?.message || e.message),
      "error",
    );
  } finally {
    loading.value = false;
  }
};

const openUploadModal = () => {
  uploadForm.department = user.value?.department || "SALES";
  showModal.value = true;
};

const onFileSelected = (e: any) => {
  const file = e.target.files[0];
  if (file) {
    selectedFile.value = file;
  }
};

const handleUpload = async () => {
  if (!selectedFile.value) return;
  uploading.value = true;

  const formData = new FormData();
  formData.append("file", selectedFile.value);
  formData.append("title", uploadForm.title);
  formData.append("department", uploadForm.department);
  formData.append("classification", uploadForm.classification);

  try {
    await $fetch("http://localhost:8080/api/documents/upload", {
      method: "POST",
      headers: { Authorization: `Bearer ${accessToken.value}` },
      body: formData,
    });
    showModal.value = false;
    showAlert("Đã mã hóa AES-256-GCM và lưu tài liệu thành công!", "success");
    await fetchDocuments();
  } catch (e: any) {
    showAlert("Lỗi upload: " + (e.data?.message || e.message), "error");
  } finally {
    uploading.value = false;
  }
};

const downloadDoc = async (doc: any) => {
  try {
    const blob = await $fetch<Blob>(
      `http://localhost:8080/api/documents/${doc.id}/download`,
      {
        headers: { Authorization: `Bearer ${accessToken.value}` },
        responseType: "blob",
      },
    );

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = doc.originalFilename;
    a.click();
    window.URL.revokeObjectURL(url);
    showAlert(`Đã giải mã thành công file #${doc.id}`, "success");
  } catch (e: any) {
    showAlert(
      `BOLA/ABAC Blocked: Bạn không có quyền truy cập/tải về tài liệu #${doc.id}`,
      "error",
    );
    console.error(e);
  }
};

const deleteDoc = async (id: number) => {
  if (!confirm(`Bạn có chắc chắn muốn xóa tài liệu #${id}?`)) return;
  try {
    await $fetch(`http://localhost:8080/api/documents/${id}`, {
      method: "DELETE",
      headers: { Authorization: `Bearer ${accessToken.value}` },
    });
    showAlert(`Đã xóa tài liệu #${id} thành công`, "success");
    await fetchDocuments();
  } catch (e: any) {
    showAlert(
      `BFLA Blocked: Backend từ chối quyền xóa tài liệu #${id}`,
      "error",
    );
    console.error(e);
  }
};

const testBolaAccess = async () => {
  if (!testDocId.value) return;
  try {
    const res = await $fetch<any>(
      `http://localhost:8080/api/documents/${testDocId.value}`,
      {
        headers: { Authorization: `Bearer ${accessToken.value}` },
      },
    );
    showAlert(
      `Kết quả API #${res.id}: ${res.title} (Bảo mật: ${res.classification})`,
      "success",
    );
  } catch (e: any) {
    showAlert(
      `HTTP ${e.statusCode || 403} Forbidden - BOLA Prevented: Hệ thống ngăn chặn truy cập ID #${testDocId.value}`,
      "error",
    );
    console.error(e);
  }
};

const showAlert = (msg: string, type: "success" | "error") => {
  alertMessage.value = msg;
  alertType.value = type;
};

const formatSize = (bytes: number) => {
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
  return (bytes / (1024 * 1024)).toFixed(1) + " MB";
};

const getClassificationBadge = (c: string) => {
  if (c === "CONFIDENTIAL") return "badge-confidential";
  if (c === "INTERNAL") return "badge-internal";
  return "badge-public";
};
</script>

<style scoped>
.doc-row:hover {
  background: rgba(255, 255, 255, 0.03);
}
</style>
