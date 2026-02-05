#!/usr/bin/env pwsh
# API Test Script for Profile Module

$BASE_URL = "http://localhost:8080"
$API_ENDPOINT = "$BASE_URL/api"

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Profile API Test Script" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

# Test 1: Register User
Write-Host "`n[TEST 1] Register User" -ForegroundColor Yellow
$registerResponse = Invoke-WebRequest -Uri "$API_ENDPOINT/auth/register" `
    -Method POST `
    -ContentType "application/json" `
    -Body '{
        "username": "testuser_'$(Get-Random)'",
        "password": "password123"
    }' | ConvertFrom-Json

$userId = $registerResponse.data.id
Write-Host "✓ User Registered: $userId" -ForegroundColor Green
Write-Host "Response: $($registerResponse | ConvertTo-Json)" -ForegroundColor Gray

# Test 2: Get Profile (should exist after registration)
Write-Host "`n[TEST 2] Get Profile" -ForegroundColor Yellow
try {
    $getProfileResponse = Invoke-WebRequest -Uri "$API_ENDPOINT/profiles/$userId" `
        -Method GET | ConvertFrom-Json
    Write-Host "✓ Profile Retrieved" -ForegroundColor Green
    Write-Host "Response: $($getProfileResponse | ConvertTo-Json)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Error getting profile: $_" -ForegroundColor Red
}

# Test 3: Update Profile
Write-Host "`n[TEST 3] Update Profile" -ForegroundColor Yellow
try {
    $updateResponse = Invoke-WebRequest -Uri "$API_ENDPOINT/profiles/$userId" `
        -Method PUT `
        -ContentType "application/json" `
        -Body '{
            "fullName": "Test User Name",
            "bio": "This is a test profile",
            "avatar": "https://example.com/avatar.jpg",
            "phoneNumber": "0912345678",
            "address": "123 Test Street"
        }' | ConvertFrom-Json
    Write-Host "✓ Profile Updated" -ForegroundColor Green
    Write-Host "Response: $($updateResponse | ConvertTo-Json)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Error updating profile: $_" -ForegroundColor Red
}

# Test 4: Get Updated Profile
Write-Host "`n[TEST 4] Get Updated Profile" -ForegroundColor Yellow
try {
    $getUpdatedResponse = Invoke-WebRequest -Uri "$API_ENDPOINT/profiles/$userId" `
        -Method GET | ConvertFrom-Json
    Write-Host "✓ Updated Profile Retrieved" -ForegroundColor Green
    Write-Host "Full Name: $($getUpdatedResponse.data.fullName)" -ForegroundColor Green
    Write-Host "Bio: $($getUpdatedResponse.data.bio)" -ForegroundColor Green
    Write-Host "Phone: $($getUpdatedResponse.data.phoneNumber)" -ForegroundColor Green
    Write-Host "Response: $($getUpdatedResponse | ConvertTo-Json)" -ForegroundColor Gray
} catch {
    Write-Host "✗ Error getting updated profile: $_" -ForegroundColor Red
}

# Test 5: Error Case - Get Non-existent Profile
Write-Host "`n[TEST 5] Error Case - Non-existent Profile" -ForegroundColor Yellow
try {
    $errorResponse = Invoke-WebRequest -Uri "$API_ENDPOINT/profiles/non-existent-id" `
        -Method GET -ErrorAction Stop | ConvertFrom-Json
    Write-Host "✗ Should have thrown 404 error" -ForegroundColor Red
} catch {
    if ($_.Exception.Response.StatusCode -eq 404) {
        Write-Host "✓ Correctly returned 404 for non-existent profile" -ForegroundColor Green
    } else {
        Write-Host "⚠ Unexpected error: $($_.Exception.Response.StatusCode)" -ForegroundColor Yellow
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Test Suite Complete!" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
