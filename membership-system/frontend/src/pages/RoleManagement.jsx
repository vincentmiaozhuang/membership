import React, { useState, useEffect } from 'react'
import { Table, Button, Modal, Form, Input, Select, message, Popconfirm } from 'antd'
import { PlusOutlined, EditOutlined, DeleteOutlined } from '@ant-design/icons'
import request from '../utils/request'

const RoleManagement = () => {
  const [roles, setRoles] = useState([])
  const [permissions, setPermissions] = useState([])
  const [loading, setLoading] = useState(false)
  const [modalVisible, setModalVisible] = useState(false)
  const [editingRole, setEditingRole] = useState(null)
  const [form] = Form.useForm()

  const fetchRoles = async () => {
    setLoading(true)
    try {
      const data = await request.get('/roles')
      setRoles(data)
    } catch (error) {
      message.error('获取角色列表失败')
    } finally {
      setLoading(false)
    }
  }

  const fetchPermissions = async () => {
    try {
      const data = await request.get('/permissions')
      setPermissions(data)
    } catch (error) {
      message.error('获取权限列表失败')
    }
  }

  useEffect(() => {
    fetchRoles()
    fetchPermissions()
  }, [])

  const handleAdd = () => {
    setEditingRole(null)
    form.resetFields()
    setModalVisible(true)
  }

  const handleEdit = (record) => {
    setEditingRole(record)
    form.setFieldsValue({
      ...record,
      permissionIds: record.permissionIds || [],
    })
    setModalVisible(true)
  }

  const handleDelete = async (id) => {
    try {
      await request.delete(`/roles/${id}`)
      message.success('删除成功')
      fetchRoles()
    } catch (error) {
      message.error('删除失败')
    }
  }

  const handleModalOk = async () => {
    try {
      const values = await form.validateFields()
      if (editingRole) {
        await request.put(`/roles/${editingRole.id}`, values)
        message.success('更新成功')
      } else {
        await request.post('/roles', values)
        message.success('创建成功')
      }
      setModalVisible(false)
      fetchRoles()
    } catch (error) {
      console.error(error)
    }
  }

  const columns = [
    {
      title: '角色名称',
      dataIndex: 'name',
      key: 'name',
    },
    {
      title: '描述',
      dataIndex: 'description',
      key: 'description',
    },
    {
      title: '权限数量',
      dataIndex: 'permissionIds',
      key: 'permissionIds',
      render: (ids) => ids?.length || 0,
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
            title="确定删除此角色吗？"
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
          新增角色
        </Button>
      </div>
      <Table
        columns={columns}
        dataSource={roles}
        rowKey="id"
        loading={loading}
      />
      <Modal
        title={editingRole ? '编辑角色' : '新增角色'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={() => setModalVisible(false)}
      >
        <Form form={form} layout="vertical">
          <Form.Item
            name="name"
            label="角色名称"
            rules={[{ required: true, message: '请输入角色名称' }]}
          >
            <Input disabled={!!editingRole} />
          </Form.Item>
          <Form.Item name="description" label="描述">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item name="permissionIds" label="权限">
            <Select mode="multiple" placeholder="请选择权限">
              {permissions.map((permission) => (
                <Select.Option key={permission.id} value={permission.id}>
                  {permission.name} - {permission.description}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

export default RoleManagement
