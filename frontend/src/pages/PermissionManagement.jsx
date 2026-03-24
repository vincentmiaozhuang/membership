import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, message, Popconfirm } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import request from '../utils/request'

const PermissionManagement = () => {
  const [permissions, setPermissions] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingPermission, setEditingPermission] = useState(null)
  const [form] = Form.useForm()

  const fetchPermissions = async () => {
    setLoading(true)
    try {
      const data = await request.get('/permissions')
      setPermissions(data)
    } catch (error) {
      message.error('获取权限列表失败')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchPermissions()
  }, [])

  const handleAdd = () => {
    setEditingPermission(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingPermission(record)
    form.setFieldsValue(record)
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await request.delete(`/permissions/${id}`)
      message.success('删除成功')
      fetchPermissions()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      if (editingPermission) {
        await request.put(`/permissions/${editingPermission.id}`, values)
        message.success('更新成功')
      } else {
        await request.post('/permissions', values)
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchPermissions()
    } catch (error) {
      console.error(error)
    }
  }

  const columns = [
    {
      title: '权限名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '资源',
      dataIndex: 'resource',
      key: 'resource',
    },
    {
      title: '操作',
      dataIndex: 'action',
      key: 'action',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '操作',
      key: 'action',
      render: (_, record) => (
        <span>
          <Button
            type="link"
            icon={<EditOutlined />}
            onClick={() => handleEdit(record)}
          >
            编辑
          </Button>
          <Popconfirm
            title="确定删除此权限吗？"
            onConfirm={() => handleDelete(record.id)}
          >
            <Button type="link" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </span>
      ),
    },
  ]

  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
          新增权限
        </Button>
      </div>
      <Table
        columns={columns}
        dataSource={permissions}
        rowKey="id"
        loading={loading}
      />
      <Modal
        title={editingPermission ? '编辑权限' : '新增权限'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="权限名称"
            rules={[{ required: true, message: '请输入权限名称' }]}
          >
            <Input disabled={!!editingPermission} />
          </Form.Item>
          <Form.Item
            name="resource"
            label="资源"
            rules={[{ required: true, message: '请输入资源' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item
            name="action"
            label="操作"
            rules={[{ required: true, message: '请输入操作' }]}
          >
            <Input />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default PermissionManagement
