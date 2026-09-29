<template>
  <div class="page-card messages">
    <div class="page-header">
      <h2 class="page-title">私信</h2>
    </div>
    <el-row :gutter="16" class="panel">
      <el-col :xs="24" :md="8" class="left">
        <div v-loading="convLoading" class="conv-list">
          <div
            v-for="c in conversations"
            :key="c.id"
            class="conv-item"
            :class="{ active: String(c.id) === String(activeId) }"
            @click="selectConv(c)"
          >
            <el-avatar :size="36" :src="c.peerAvatar || undefined">{{ (c.peerName || '?').charAt(0) }}</el-avatar>
            <div class="conv-info">
              <div class="name">
                {{ c.peerName || `用户${c.peerId}` }}
                <el-badge v-if="c.unread" :value="c.unread" />
              </div>
              <div class="last">{{ c.lastMessage || '暂无消息' }}</div>
            </div>
          </div>
          <el-empty v-if="!conversations.length && !convLoading" description="暂无会话" :image-size="64" />
        </div>
      </el-col>
      <el-col :xs="24" :md="16" class="right">
        <template v-if="activeId || peerId">
          <div class="chat-title">
            {{ activePeerName || '会话' }}
          </div>
          <div ref="chatBox" v-loading="msgLoading" class="chat-box">
            <div
              v-for="m in messages"
              :key="m.id"
              class="bubble-row"
              :class="{ mine: isMine(m) }"
            >
              <div class="bubble">
                <div>{{ m.content }}</div>
                <div class="time">{{ formatDate(m.createTime) }}</div>
              </div>
            </div>
            <el-empty v-if="!messages.length && !msgLoading" description="开始聊天吧" :image-size="48" />
          </div>
          <div class="composer">
            <el-input
              v-model="draft"
              type="textarea"
              :rows="2"
              placeholder="输入消息，Enter 发送（Shift+Enter 换行）"
              @keydown.enter.exact.prevent="send"
            />
            <el-button type="primary" :loading="sending" @click="send">发送</el-button>
          </div>
        </template>
        <el-empty v-else description="选择左侧会话开始聊天" />
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  getConversationsApi,
  getMessagesApi,
  sendMessageApi,
  startConversationApi,
} from '@/api/message'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils/format'
import type { Conversation, Message } from '@/utils/types'

const route = useRoute()
const userStore = useUserStore()

const conversations = ref<Conversation[]>([])
const messages = ref<Message[]>([])
const convLoading = ref(false)
const msgLoading = ref(false)
const sending = ref(false)
const activeId = ref<string | number | ''>('')
const peerId = ref<string | number | ''>('')
const activePeerName = ref('')
const draft = ref('')
const chatBox = ref<HTMLElement>()

const myId = computed(() => userStore.userInfo?.id)

function isMine(m: Message) {
  return String(m.senderId) === String(myId.value)
}

async function loadConversations() {
  convLoading.value = true
  try {
    const data = await getConversationsApi()
    conversations.value = Array.isArray(data) ? data : []
  } catch {
    conversations.value = []
  } finally {
    convLoading.value = false
  }
}

async function loadMessages() {
  if (!activeId.value) {
    messages.value = []
    return
  }
  msgLoading.value = true
  try {
    const data = await getMessagesApi(activeId.value)
    messages.value = Array.isArray(data) ? data : []
    await nextTick()
    if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
  } catch {
    messages.value = []
  } finally {
    msgLoading.value = false
  }
}

function selectConv(c: Conversation) {
  activeId.value = c.id
  peerId.value = c.peerId
  activePeerName.value = c.peerName || `用户${c.peerId}`
  loadMessages()
}

async function send() {
  const content = draft.value.trim()
  if (!content) return
  if (!peerId.value && !activeId.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  sending.value = true
  try {
    const msg = await sendMessageApi({
      receiverId: peerId.value,
      content,
      conversationId: activeId.value || undefined,
    })
    messages.value.push(
      msg || {
        id: Date.now(),
        senderId: myId.value!,
        content,
        createTime: new Date().toISOString(),
      }
    )
    draft.value = ''
    await nextTick()
    if (chatBox.value) chatBox.value.scrollTop = chatBox.value.scrollHeight
    loadConversations()
  } catch (e: any) {
    ElMessage.error(e?.message || '发送失败，请检查网络或后端服务')
  } finally {
    sending.value = false
  }
}

async function initFromQuery() {
  const qId = route.query.conversationId as string | undefined
  const qPeer = route.query.peerId as string | undefined
  const qName = (route.query.peerName as string) || ''
  if (qId) {
    activeId.value = qId
    // conversationId 与 peer 用户 id 一致时，缺 peerId 也要用上，避免首次发送 receiverId 为空
    peerId.value = qPeer || qId
    activePeerName.value = qName || (peerId.value ? `用户${peerId.value}` : '')
    loadMessages()
  } else if (qPeer) {
    peerId.value = qPeer
    activePeerName.value = qName || `用户${qPeer}`
    try {
      const conv = await startConversationApi(qPeer)
      activeId.value = conv.id
      peerId.value = conv.peerId || qPeer
      activePeerName.value = conv.peerName || activePeerName.value
      await loadConversations()
      loadMessages()
    } catch {
      // 无后端时保留临时会话
      activeId.value = `temp-${qPeer}`
      conversations.value = [
        {
          id: activeId.value,
          peerId: qPeer,
          peerName: activePeerName.value,
          lastMessage: '',
        },
        ...conversations.value,
      ]
    }
  }
}

watch(activeId, () => {
  const c = conversations.value.find((x) => String(x.id) === String(activeId.value))
  if (c) {
    peerId.value = c.peerId
    activePeerName.value = c.peerName || activePeerName.value
  }
})

onMounted(async () => {
  await loadConversations()
  await initFromQuery()
})
</script>

<style scoped>
.panel {
  min-height: 480px;
}
.left {
  border-right: 1px solid #ebeef5;
  min-height: 480px;
}
.conv-list {
  max-height: 520px;
  overflow: auto;
}
.conv-item {
  display: flex;
  gap: 10px;
  padding: 12px;
  cursor: pointer;
  border-radius: 6px;
}
.conv-item:hover,
.conv-item.active {
  background: #ecf5ff;
}
.conv-info {
  flex: 1;
  min-width: 0;
}
.name {
  display: flex;
  justify-content: space-between;
  font-weight: 600;
  font-size: 14px;
}
.last {
  color: #909399;
  font-size: 12px;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.chat-title {
  font-weight: 600;
  padding-bottom: 8px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 8px;
}
.chat-box {
  height: 360px;
  overflow: auto;
  padding: 8px;
  background: #fafafa;
  border-radius: 6px;
}
.bubble-row {
  display: flex;
  margin-bottom: 10px;
}
.bubble-row.mine {
  justify-content: flex-end;
}
.bubble {
  max-width: 70%;
  background: #fff;
  border: 1px solid #ebeef5;
  border-radius: 8px;
  padding: 8px 12px;
  font-size: 14px;
}
.bubble-row.mine .bubble {
  background: #409eff;
  color: #fff;
  border-color: #409eff;
}
.time {
  font-size: 11px;
  opacity: 0.7;
  margin-top: 4px;
}
.composer {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  align-items: flex-end;
}
</style>
